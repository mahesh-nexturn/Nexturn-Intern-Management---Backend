terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source                = "hashicorp/aws"
      version               = "~> 5.60"
      configuration_aliases = [aws.us_east_1] # CloudFront certificates must live in us-east-1
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.6"
    }
  }
}
