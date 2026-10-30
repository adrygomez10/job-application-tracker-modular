#!/usr/bin/env bash
set -euo pipefail

load_workshop() {
  for tool in terraform aws kubectl helm jq; do
    command -v "$tool" >/dev/null || {
      echo "Missing tool: $tool" >&2
      exit 1
    }
  done
  local outputs current_account
  outputs=$(terraform -chdir=infra output -json)
  AWS_REGION=$(jq -er '.region.value' <<<"$outputs")
  AWS_CLUSTER_NAME=$(jq -er '.cluster_name.value' <<<"$outputs")
  AWS_ACCOUNT_ID=$(jq -er '.account_id.value' <<<"$outputs")
  WORKSHOP_VPC_ID=$(jq -er '.vpc_id.value' <<<"$outputs")
  ALB_ROLE_ARN=$(jq -er '.alb_role_arn.value' <<<"$outputs")
  current_account=$(aws sts get-caller-identity --query Account --output text)
  [[ "$current_account" == "$AWS_ACCOUNT_ID" ]] || {
    echo "Wrong AWS account." >&2
    exit 1
  }
  WORKSHOP_CONTEXT="arn:aws:eks:$AWS_REGION:$AWS_ACCOUNT_ID:cluster/$AWS_CLUSTER_NAME"
  export AWS_REGION AWS_CLUSTER_NAME AWS_ACCOUNT_ID WORKSHOP_VPC_ID WORKSHOP_CONTEXT ALB_ROLE_ARN
}
