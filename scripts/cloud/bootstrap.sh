#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/../.."
source scripts/cloud/common.sh
load_workshop
aws eks update-kubeconfig --region "$AWS_REGION" --name "$AWS_CLUSTER_NAME"
kubectl --context "$WORKSHOP_CONTEXT" apply -f deploy/platform.yaml
helm repo add eks https://aws.github.io/eks-charts --force-update
helm repo update eks
chart=$(mktemp)
trap 'rm -f "$chart"' EXIT
curl --fail --silent --show-error --location \
  https://aws.github.io/eks-charts/aws-load-balancer-controller-3.6.2.tgz -o "$chart"
printf '%s  %s\n' 9255bf39873b6cb31106dbe61dc87c3617bff4197783b5b94381811a13f99c3c "$chart" | sha256sum --check
helm upgrade --install aws-load-balancer-controller "$chart" \
  --kube-context "$WORKSHOP_CONTEXT" --namespace kube-system \
  --set-string clusterName="$AWS_CLUSTER_NAME" \
  --set-string region="$AWS_REGION" \
  --set-string vpcId="$WORKSHOP_VPC_ID" \
  --set-string "serviceAccount.annotations.eks\.amazonaws\.com/role-arn=$ALB_ROLE_ARN" \
  --wait --timeout 10m
kubectl --context "$WORKSHOP_CONTEXT" -n kube-system rollout status deployment/aws-load-balancer-controller --timeout=120s
echo "Platform ready. Create the runtime Secret before enabling delivery."
