terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.60"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.6"
    }
  }

  # Each environment has its OWN state. Until the shared backend exists this uses
  # a local terraform.tfstate in this folder. For team use, create the bucket +
  # lock table once, then uncomment and run `terraform init -migrate-state`.
  # backend "s3" {
  #   bucket         = "REPLACE-tfstate-bucket"
  #   key            = "intern-management/dev/terraform.tfstate"
  #   region         = "ap-south-1"
  #   dynamodb_table = "REPLACE-tfstate-lock"
  #   encrypt        = true
  # }
}

# No credentials in code: uses env vars, `aws configure`, or an AWS profile (see ../../README.md).
provider "aws" {
  region  = var.aws_region
  profile = var.aws_profile == "" ? null : var.aws_profile

  default_tags {
    tags = { Project = "intern-mgmt", Environment = "dev", ManagedBy = "terraform" }
  }
}

provider "aws" {
  alias   = "us_east_1"
  region  = "us-east-1"
  profile = var.aws_profile == "" ? null : var.aws_profile

  default_tags {
    tags = { Project = "intern-mgmt", Environment = "dev", ManagedBy = "terraform" }
  }
}

variable "aws_region" {
  type    = string
  default = "ap-south-1"
}

variable "aws_profile" {
  description = "Optional AWS CLI profile; empty = default credential chain."
  type        = string
  default     = ""
}

variable "domain_name" {
  type    = string
  default = ""
}

variable "route53_zone_id" {
  type    = string
  default = ""
}

variable "notification_emails" {
  type    = list(string)
  default = []
}

variable "image_tag" {
  type    = string
  default = "latest"
}

# Dev: small and cheap, easy to destroy.
module "app" {
  source = "../../modules/app"
  providers = {
    aws           = aws
    aws.us_east_1 = aws.us_east_1
  }

  project     = "intern-mgmt"
  environment = "dev"
  aws_region  = var.aws_region
  vpc_cidr    = "10.10.0.0/16"

  domain_name     = var.domain_name
  route53_zone_id = var.route53_zone_id
  image_tag       = var.image_tag

  task_cpu      = 256
  task_memory   = 512
  desired_count = 1
  min_capacity  = 1
  max_capacity  = 2

  db_instance_class        = "db.t4g.micro"
  db_multi_az              = false
  db_backup_retention_days = 1
  db_deletion_protection   = false

  log_retention_days  = 7
  notification_emails = var.notification_emails
}

output "site_url" { value = module.app.site_url }
output "cloudfront_distribution_id" { value = module.app.cloudfront_distribution_id }
output "frontend_bucket" { value = module.app.frontend_bucket }
output "ecr_repository_url" { value = module.app.ecr_repository_url }
output "ecs_cluster" { value = module.app.ecs_cluster }
output "ecs_service" { value = module.app.ecs_service }
output "uploads_bucket" { value = module.app.uploads_bucket }
output "sns_topic_arn" { value = module.app.sns_topic_arn }
output "db_endpoint" { value = module.app.db_endpoint }
