#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/../.."
source scripts/cloud/common.sh
load_workshop
echo "AWS account: $AWS_ACCOUNT_ID; region: $AWS_REGION; cluster: $AWS_CLUSTER_NAME"
echo "This deletes the disposable workshop database. It does not affect local Compose data."
read -r -p "Type DELETE-DISPOSABLE-DATA to continue: " confirmation
[[ "$confirmation" == "DELETE-DISPOSABLE-DATA" ]] || exit 1
# Disable AWS_DEPLOY_ENABLED and wait for running delivery jobs before this command.
kubectl --context "$WORKSHOP_CONTEXT" -n tracker delete ingress tracker --ignore-not-found --wait=true --timeout=10m
volume_ids=$(kubectl --context "$WORKSHOP_CONTEXT" get pv -o json \
  | jq -r '.items[] | select(.spec.claimRef.namespace == "tracker") | .spec.csi.volumeHandle // empty')
releases=$(helm list --kube-context "$WORKSHOP_CONTEXT" -n tracker --all --output json)
if jq -e 'any(.[]; .name == "tracker")' <<<"$releases" >/dev/null; then
  helm uninstall tracker --kube-context "$WORKSHOP_CONTEXT" -n tracker --wait --timeout 10m
fi
# Helm deliberately retains the PVC; deletion here is the explicit disposal of its data.
kubectl --context "$WORKSHOP_CONTEXT" -n tracker delete pvc tracker-db-data --ignore-not-found --wait=true --timeout=10m
for ((attempt = 0; attempt < 60; attempt++)); do
  remaining=$(kubectl --context "$WORKSHOP_CONTEXT" get pv \
    -o json | jq '[.items[] | select(.spec.claimRef.namespace == "tracker")] | length')
  [[ "$remaining" != 0 ]] || break
  sleep 5
done
[[ "$remaining" == 0 ]] || {
  echo "Database PV still exists. Keep EBS CSI active and investigate." >&2
  exit 1
}
for volume in $volume_ids; do
  aws ec2 wait volume-deleted --region "$AWS_REGION" --volume-ids "$volume"
done
# Only read resources inside this dedicated VPC. Never delete unrelated account resources.
for ((attempt = 0; attempt < 60; attempt++)); do
  albs=$(aws elbv2 describe-load-balancers --region "$AWS_REGION" --output json \
    | jq --arg vpc "$WORKSHOP_VPC_ID" '[.LoadBalancers[] | select(.VpcId == $vpc)] | length')
  [[ "$albs" != 0 ]] || break
  sleep 5
done
[[ "$albs" == 0 ]] || {
  echo "A load balancer remains in the VPC. Stop and investigate." >&2
  exit 1
}
terraform -chdir=infra plan -destroy -out=destroy.tfplan
terraform -chdir=infra apply destroy.tfplan
echo "Destroy completed. Follow the AWS inventory checks in README.md."
