mock_provider "aws" {
  mock_resource "aws_launch_template" {
    defaults = { id = "lt-0123456789abcdef0", latest_version = 1 }
  }
  mock_resource "aws_iam_role" {
    defaults = { arn = "arn:aws:iam::123456789012:role/mock-role" }
  }
  mock_resource "aws_iam_policy" {
    defaults = { arn = "arn:aws:iam::123456789012:policy/mock-policy" }
  }
  mock_resource "aws_ecr_repository" {
    defaults = { arn = "arn:aws:ecr:eu-west-1:123456789012:repository/tracker-workshop" }
  }
  mock_data "aws_eks_addon_version" {
    defaults = { version = "v1.0.0-eksbuild.1" }
  }
  mock_data "aws_caller_identity" {
    defaults = { account_id = "123456789012" }
  }
  mock_data "aws_partition" {
    defaults = { partition = "aws" }
  }
  mock_data "aws_availability_zones" {
    defaults = { names = ["eu-west-1a", "eu-west-1b"] }
  }
  mock_resource "aws_eks_cluster" {
    defaults = {
      name     = "tracker-workshop"
      arn      = "arn:aws:eks:eu-west-1:123456789012:cluster/tracker-workshop"
      identity = [{ oidc = [{ issuer = "https://oidc.eks.eu-west-1.amazonaws.com/id/TEST" }] }]
    }
  }
}
variables {
  create_github_oidc_provider = true
  app_hostname                = "tracker.example.test"
  admin_principal_arn         = "arn:aws:iam::123456789012:role/workshop-admin"
  github_oidc_subject         = "repo:example/tracker:environment:aws-workshop"
}
run "identity_and_resource_boundaries" {
  command = apply
  assert {
    condition = (
      jsondecode(aws_iam_role.delivery.assume_role_policy).Statement[0].Condition.StringEquals["token.actions.githubusercontent.com:sub"] == var.github_oidc_subject &&
      jsondecode(aws_iam_role.delivery.assume_role_policy).Statement[0].Condition.StringEquals["token.actions.githubusercontent.com:aud"] == "sts.amazonaws.com"
    )
    error_message = "Delivery must require the exact repository/environment subject and AWS audience."
  }
  assert {
    condition     = aws_eks_access_entry.delivery.kubernetes_groups == toset(["tracker-deploy"])
    error_message = "Delivery must use its namespace-scoped RBAC group."
  }
  assert {
    condition     = jsondecode(aws_iam_role_policy.delivery.policy).Statement[1].Resource == aws_ecr_repository.app.arn
    error_message = "Publishing must be restricted to the workshop repository."
  }
  assert {
    condition = (
      jsondecode(aws_iam_role_policy.delivery.policy).Statement[2].Resource == aws_eks_cluster.workshop.arn &&
      jsondecode(aws_iam_role_policy.delivery.policy).Statement[2].Action == ["eks:DescribeCluster"]
    )
    error_message = "Delivery must not receive cluster provisioning permissions."
  }
  assert {
    condition = (
      aws_launch_template.nodes.metadata_options[0].http_tokens == "required" &&
      aws_launch_template.nodes.metadata_options[0].http_put_response_hop_limit == 1
    )
    error_message = "Require IMDSv2 and prevent normal workload pods from accessing node credentials."
  }
  assert {
    condition     = aws_eks_cluster.workshop.upgrade_policy[0].support_type == "STANDARD"
    error_message = "The disposable cluster must not opt into more expensive extended support."
  }
}
