variable "aws_region" {
  type    = string
  default = "ap-south-1"
}

variable "project" {
  type    = string
  default = "intern-mgmt"
}

variable "environment" {
  type    = string
  default = "prod"
}

# ---------- Network ----------
variable "vpc_cidr" {
  type    = string
  default = "10.0.0.0/16"
}

variable "az_count" {
  type    = number
  default = 2
}

# ---------- Domain / HTTPS ----------
variable "domain_name" {
  description = "Custom domain for the site (e.g. intern.example.com). Empty = use the default *.cloudfront.net HTTPS URL."
  type        = string
  default     = ""
}

variable "route53_zone_id" {
  description = "Route53 hosted zone ID for domain_name. Required when domain_name is set (used for ACM DNS validation + alias record)."
  type        = string
  default     = ""
}

# ---------- Backend ----------
variable "container_port" {
  type    = number
  default = 8080
}

variable "image_tag" {
  type    = string
  default = "latest"
}

variable "task_cpu" {
  type    = number
  default = 512
}

variable "task_memory" {
  type    = number
  default = 1024
}

variable "desired_count" {
  type    = number
  default = 2
}

variable "min_capacity" {
  type    = number
  default = 2
}

variable "max_capacity" {
  type    = number
  default = 6
}

variable "cpu_target_percent" {
  type    = number
  default = 60
}

variable "health_check_path" {
  description = "Needs spring-boot-starter-actuator in the backend (see README)."
  type        = string
  default     = "/actuator/health"
}

variable "log_retention_days" {
  type    = number
  default = 30
}

# ---------- Database ----------
variable "db_name" {
  type    = string
  default = "intern_management"
}

variable "db_username" {
  type    = string
  default = "internadmin"
}

variable "db_instance_class" {
  type    = string
  default = "db.t4g.micro"
}

variable "db_allocated_storage" {
  type    = number
  default = 20
}

variable "db_max_allocated_storage" {
  type    = number
  default = 100
}

variable "db_engine_version" {
  type    = string
  default = "16"
}

variable "db_multi_az" {
  type    = bool
  default = false
}

variable "db_backup_retention_days" {
  type    = number
  default = 7
}

variable "db_deletion_protection" {
  type    = bool
  default = true
}

# ---------- Notifications ----------
variable "notification_emails" {
  description = "Emails subscribed to the SNS topic. Each person must click the confirmation link."
  type        = list(string)
  default     = []
}
