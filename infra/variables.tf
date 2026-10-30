variable "aws_region" {
  type    = string
  default = "eu-west-1"
}
variable "name" {
  type    = string
  default = "tracker-workshop"
  validation {
    condition     = can(regex("^[a-z][a-z0-9-]{2,24}$", var.name))
    error_message = "Use 3-25 lowercase letters, digits and hyphens."
  }
}
variable "kubernetes_version" {
  type    = string
  default = "1.35"
}
variable "admin_principal_arn" {
  description = "Permanent IAM role or user ARN used locally, never an STS session ARN."
  type        = string
  validation {
    condition     = can(regex("^arn:aws:iam::[0-9]{12}:(role|user)/.+$", var.admin_principal_arn))
    error_message = "Provide an IAM role or user ARN, not an STS assumed-role ARN."
  }
}
variable "github_oidc_subject" {
  description = "Exact sub claim for this repository and the aws-workshop environment."
  type        = string
  validation {
    condition     = startswith(var.github_oidc_subject, "repo:") && endswith(var.github_oidc_subject, ":environment:aws-workshop") && !strcontains(var.github_oidc_subject, "*")
    error_message = "Use the exact repository subject ending in :environment:aws-workshop, without wildcards."
  }
}
variable "create_github_oidc_provider" {
  description = "False when the account already has the GitHub OIDC provider."
  type        = bool
  default     = true
}
variable "instance_type" {
  type    = string
  default = "c7i-flex.large"
}
variable "public_api_cidrs" {
  description = "Public API endpoint ranges. Hosted GitHub runners need public access; IAM and RBAC still apply."
  type        = list(string)
  default     = ["0.0.0.0/0"]
  validation {
    condition     = length(var.public_api_cidrs) > 0 && alltrue([for cidr in var.public_api_cidrs : can(cidrnetmask(cidr))])
    error_message = "Provide at least one valid IPv4 CIDR."
  }
}
