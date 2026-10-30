locals {
  eks_oidc_issuer = replace(aws_eks_cluster.workshop.identity[0].oidc[0].issuer, "https://", "")
  service_accounts = {
    cni = "system:serviceaccount:kube-system:aws-node"
    ebs = "system:serviceaccount:kube-system:ebs-csi-controller-sa"
    alb = "system:serviceaccount:kube-system:aws-load-balancer-controller"
  }
}
resource "aws_iam_openid_connect_provider" "eks" {
  url            = aws_eks_cluster.workshop.identity[0].oidc[0].issuer
  client_id_list = ["sts.amazonaws.com"]
}
resource "aws_iam_role" "service_accounts" {
  for_each = local.service_accounts
  name     = "${var.name}-${each.key}"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Principal = { Federated = aws_iam_openid_connect_provider.eks.arn }
      Action    = "sts:AssumeRoleWithWebIdentity"
      Condition = {
        StringEquals = {
          "${local.eks_oidc_issuer}:aud" = "sts.amazonaws.com"
          "${local.eks_oidc_issuer}:sub" = each.value
        }
      }
    }]
  })
}
resource "aws_iam_role_policy_attachment" "cni" {
  role       = aws_iam_role.service_accounts["cni"].name
  policy_arn = "arn:${local.partition}:iam::aws:policy/AmazonEKS_CNI_Policy"
}
resource "aws_iam_role_policy_attachment" "ebs" {
  role       = aws_iam_role.service_accounts["ebs"].name
  policy_arn = "arn:${local.partition}:iam::aws:policy/service-role/AmazonEBSCSIDriverPolicy"
}
resource "aws_iam_policy" "alb" {
  name   = "${var.name}-alb-controller"
  policy = file("${path.module}/policies/alb-controller.json")
}
resource "aws_iam_role_policy_attachment" "alb" {
  role       = aws_iam_role.service_accounts["alb"].name
  policy_arn = aws_iam_policy.alb.arn
}
data "aws_eks_addon_version" "addons" {
  for_each           = toset(["vpc-cni", "coredns", "kube-proxy", "aws-ebs-csi-driver"])
  addon_name         = each.value
  kubernetes_version = var.kubernetes_version
  most_recent        = true
}
resource "aws_eks_addon" "cni" {
  cluster_name                = aws_eks_cluster.workshop.name
  addon_name                  = "vpc-cni"
  addon_version               = data.aws_eks_addon_version.addons["vpc-cni"].version
  service_account_role_arn    = aws_iam_role.service_accounts["cni"].arn
  resolve_conflicts_on_create = "OVERWRITE"
  depends_on                  = [aws_iam_role_policy_attachment.cni]
}
resource "aws_eks_addon" "remaining" {
  for_each                    = toset(["coredns", "kube-proxy", "aws-ebs-csi-driver"])
  cluster_name                = aws_eks_cluster.workshop.name
  addon_name                  = each.value
  addon_version               = data.aws_eks_addon_version.addons[each.value].version
  service_account_role_arn    = each.value == "aws-ebs-csi-driver" ? aws_iam_role.service_accounts["ebs"].arn : null
  resolve_conflicts_on_create = "OVERWRITE"
  depends_on                  = [aws_eks_node_group.workshop, aws_iam_role_policy_attachment.ebs]
}
