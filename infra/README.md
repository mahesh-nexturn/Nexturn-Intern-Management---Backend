# AWS deployment (Terraform)

Creates: private S3 + CloudFront (OAC, HTTPS, SPA routing) for React; ECR + ECS Fargate + ALB + autoscaling for Spring Boot; RDS PostgreSQL (private, encrypted, backups, Secrets Manager); private S3 uploads bucket; SNS email topic. Region default: `ap-south-1`.

## Layout: dev and prod are fully separate
```
infra\modules\app\          shared code (all resources)
infra\environments\dev\     dev settings + its own state  (small, 1 task, no deletion protection)
infra\environments\prod\    prod settings + its own state (2-6 tasks, Multi-AZ DB, deletion protection, 14-day backups)
```
Resource names include the environment (`intern-mgmt-dev-*`, `intern-mgmt-prod-*`) and the VPCs use different CIDRs (10.10/16 vs 10.20/16), so both can live in one AWS account. Best practice is a separate AWS account per environment (use a different `aws_profile` in each `terraform.tfvars`). Change environment-specific sizing in each `environments\<env>\main.tf`.

Traffic flow: `https://<site>/` -> CloudFront -> S3. `https://<site>/api/*` -> CloudFront -> ALB (only CloudFront can reach it) -> ECS -> RDS.

## 1. Adding your AWS login later (nothing is stored in code)

You need an AWS account and an IAM user/role with permission to create the resources above (AdministratorAccess for the first run is simplest). Then pick ONE:

```powershell
# A) Access keys
aws configure                       # enter key, secret, region ap-south-1

# B) SSO (company account)
aws configure sso
aws sso login --profile nexturn     # then set aws_profile = "nexturn" in environments\<env>\terraform.tfvars

# C) Environment variables (current terminal only)
$env:AWS_ACCESS_KEY_ID="..."; $env:AWS_SECRET_ACCESS_KEY="..."; $env:AWS_DEFAULT_REGION="ap-south-1"
```

Verify with `aws sts get-caller-identity`. Install Terraform (>= 1.5), AWS CLI, Docker, Node first.

## 2. Backend changes (done)
- Added `spring-boot-starter-actuator` (health check at `/actuator/health`) and AWS SDK v2 `s3` + `sns`.
- Uploads go through a `FileStorage` abstraction: `S3FileStorage` when `APP_S3_BUCKET` is set, `LocalFileStorage` otherwise (local dev unchanged). Upload/download/delete still go through the authenticated `/api/documents` endpoints.
- `NotificationService` publishes to SNS when `APP_SNS_TOPIC_ARN` is set (failures are logged, never block the request). Every email subscriber receives every notification, regardless of recipient role.
- `SERVER_PORT` is set in the task because `application.properties` overrides the port to 8082.
- Credentials come from the ECS task role automatically.
- `Dockerfile` is in the backend folder.

## 3. Deploy
```powershell
cd infra\environments\dev      # or infra\environments\prod
# edit terraform.tfvars (emails, domain, profile)
terraform init
terraform plan
terraform apply
```
Then push the first image (tasks stay unhealthy until it exists):
```powershell
$repo = terraform output -raw ecr_repository_url
aws ecr get-login-password --region ap-south-1 | docker login --username AWS --password-stdin $repo.Split('/')[0]
docker build -t "${repo}:latest" ..\..\..\Nexturn-Intern-Management---Backend
docker push "${repo}:latest"
aws ecs update-service --cluster (terraform output -raw ecs_cluster) --service (terraform output -raw ecs_service) --force-new-deployment
```
Frontend (same for dev and prod; set `VITE_API_BASE_URL=/api` in `.env.production` first; for prod replace `dev` with `prod` in the paths):
```powershell
cd ..\..\..\Nexturn-Intern-Management---Frontend
npm ci; npm run build
aws s3 sync dist s3://$(terraform -chdir=..\infra\environments\dev output -raw frontend_bucket) --delete
aws cloudfront create-invalidation --distribution-id $(terraform -chdir=..\infra\environments\dev output -raw cloudfront_distribution_id) --paths "/*"
```

## 4. SNS email confirmation
After `apply`, each address in `notification_emails` receives "AWS Notification - Subscription Confirmation". The recipient MUST click **Confirm subscription**; until then the status is `PendingConfirmation` and no notifications are delivered (check with the SNS console or `aws sns list-subscriptions-by-topic`). Terraform cannot confirm it for you. Check spam folders.

## 5. Notes
- HTTPS: with `domain_name` + `route53_zone_id` an ACM cert (us-east-1) is created and DNS-validated. Without them you get the default `*.cloudfront.net` HTTPS URL. If DNS is outside Route53, create the cert manually and adjust `modules\app\frontend.tf`.
- CloudFront -> ALB uses HTTP inside AWS, restricted by the CloudFront prefix list. For end-to-end TLS add an ALB HTTPS listener with a regional ACM cert.
- Single NAT gateway (cost). RDS Multi-AZ is on in prod, off in dev.
- Prod DB deletion protection is on; set `db_deletion_protection = false` in `environments\prod\main.tf` and apply before `terraform destroy`.
- Remote state backend is in each environment `main.tf` (commented, one state key per env) - enable it for team use.
- Security follow-ups from the earlier code review (rotate refresh tokens, restrict H2/Swagger) still apply; the H2 console and Swagger are permitted by `SecurityConfig`.
- Terraform was not installed here, so `terraform validate` has not been run; run `terraform fmt` and `terraform validate` first.
