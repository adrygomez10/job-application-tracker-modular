#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/../.."
: "${AWS_REGION:?Set AWS_REGION}"
: "${AWS_ACCOUNT_ID:?Set AWS_ACCOUNT_ID}"
: "${AWS_CLUSTER_NAME:?Set AWS_CLUSTER_NAME}"
: "${ECR_REPOSITORY:?Set ECR_REPOSITORY}"
: "${TRACKER_HOST:?Set TRACKER_HOST}"
: "${CERTIFICATE_ARN:?Set CERTIFICATE_ARN}"
: "${IMAGE_DIGEST:?Set IMAGE_DIGEST}"
: "${SOURCE_COMMIT:?Set SOURCE_COMMIT}"
[[ "$AWS_ACCOUNT_ID" =~ ^[0-9]{12}$ && "$AWS_REGION" =~ ^[a-z0-9-]+$ ]] || exit 1
[[ "$AWS_CLUSTER_NAME" =~ ^[a-z][a-z0-9-]+$ && "$ECR_REPOSITORY" =~ ^[a-z0-9/-]+$ ]] || exit 1
[[ "$IMAGE_DIGEST" =~ ^sha256:[a-f0-9]{64}$ && "$SOURCE_COMMIT" =~ ^[a-f0-9]{40}$ ]] || exit 1
[[ "$TRACKER_HOST" =~ ^[a-z0-9]([a-z0-9.-]*[a-z0-9])?$ && "$TRACKER_HOST" == *.* ]] || exit 1
[[ "$CERTIFICATE_ARN" =~ ^arn:aws:acm:$AWS_REGION:$AWS_ACCOUNT_ID:certificate/[a-zA-Z0-9-]+$ ]] || exit 1
context="arn:aws:eks:$AWS_REGION:$AWS_ACCOUNT_ID:cluster/$AWS_CLUSTER_NAME"
repository="$AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$ECR_REPOSITORY"
aws eks update-kubeconfig --region "$AWS_REGION" --name "$AWS_CLUSTER_NAME"
kubectl --context "$context" -n tracker get secret tracker-runtime -o name >/dev/null
helm upgrade --install tracker deploy/helm/tracker \
  --kube-context "$context" --namespace tracker \
  --set-string image.repository="$repository" \
  --set-string image.digest="$IMAGE_DIGEST" \
  --set-string commit="$SOURCE_COMMIT" \
  --set-string ingress.host="$TRACKER_HOST" \
  --set-string ingress.certificateArn="$CERTIFICATE_ARN" \
  --rollback-on-failure --wait --timeout 10m --history-max 5
kubectl --context "$context" -n tracker rollout status deployment/tracker-app --timeout=120s
kubectl --context "$context" -n tracker get deployment,statefulset,pods,service,ingress,pvc
# DNS is configured after the first delivery. Test the ALB directly using SNI and Host.
alb=""
for ((attempt = 0; attempt < 60; attempt++)); do
  alb=$(kubectl --context "$context" -n tracker get ingress tracker -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')
  [[ -z "$alb" ]] || break
  sleep 5
done
[[ -n "$alb" ]] || {
  echo "ALB address was not assigned." >&2
  exit 1
}
curl --fail --silent --show-error --retry 20 --retry-delay 5 --retry-all-errors \
  --connect-timeout 10 --max-time 20 \
  --connect-to "$TRACKER_HOST:443:$alb:443" \
  "https://$TRACKER_HOST/actuator/health/readiness"
printf '\nDelivered commit %s, image %s@%s\nALB: %s\n' "$SOURCE_COMMIT" "$repository" "$IMAGE_DIGEST" "$alb"
