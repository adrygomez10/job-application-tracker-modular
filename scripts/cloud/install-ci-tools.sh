#!/usr/bin/env bash
set -euo pipefail
work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT
cd "$work"
curl -fsSLo actionlint.tar.gz https://github.com/rhysd/actionlint/releases/download/v1.7.12/actionlint_1.7.12_linux_amd64.tar.gz
curl -fsSLo kubeconform.tar.gz https://github.com/yannh/kubeconform/releases/download/v0.8.0/kubeconform-linux-amd64.tar.gz
curl -fsSLo shellcheck.tar.gz https://github.com/koalaman/shellcheck/releases/download/v0.11.0/shellcheck-v0.11.0.linux.x86_64.tar.gz
curl -fsSLo shfmt https://github.com/mvdan/sh/releases/download/v3.14.1/shfmt_v3.14.1_linux_amd64
printf '%s  actionlint.tar.gz\n' 8aca8db96f1b94770f1b0d72b6dddcb1ebb8123cb3712530b08cc387b349a3d8 | sha256sum --check
printf '%s  kubeconform.tar.gz\n' 9bc2bffbf71f261128533edaf912153948b7ff238f9a531ae6d34466ec287883 | sha256sum --check
printf '%s  shellcheck.tar.gz\n' b7af85e41cc99489dcc21d66c6d5f3685138f06d34651e6d34b42ec6d54fe6f6 | sha256sum --check
printf '%s  shfmt\n' 76e77641faa025814b77f153b29796b8e6fa2fca03e0c76a691608b86c7ea7bf | sha256sum --check
tar -xzf actionlint.tar.gz actionlint
tar -xzf kubeconform.tar.gz kubeconform
tar -xzf shellcheck.tar.gz shellcheck-v0.11.0/shellcheck
sudo install -m 0755 actionlint kubeconform shfmt shellcheck-v0.11.0/shellcheck /usr/local/bin/
