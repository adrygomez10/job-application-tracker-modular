variable "app_hostname" {
  description = "A subdomain you control; DNS records are created manually from the outputs."
  type        = string
  validation {
    condition     = can(regex("^[a-z0-9][a-z0-9.-]+\\.[a-z]{2,}$", var.app_hostname))
    error_message = "Provide a lowercase DNS hostname you control."
  }
}
resource "aws_acm_certificate" "app" {
  domain_name       = var.app_hostname
  validation_method = "DNS"
}
output "app_hostname" { value = var.app_hostname }
output "certificate_arn" { value = aws_acm_certificate.app.arn }
output "certificate_validation_records" {
  value = [
    for item in aws_acm_certificate.app.domain_validation_options : {
      name  = item.resource_record_name
      type  = item.resource_record_type
      value = item.resource_record_value
    }
  ]
}
