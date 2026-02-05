# Deploying Security Shepherd on AWS

A simplified guide for deploying OWASP Security Shepherd on AWS. Choose between a quick single-server setup or a production-ready scalable deployment.

## Table of Contents
- [Quick Start: Single EC2 Instance](#quick-start-single-ec2-instance)
- [Production Deployment: ECS with Fargate](#production-deployment-ecs-with-fargate)
- [Security & Maintenance](#security--maintenance)
- [Troubleshooting](#troubleshooting)

---

## Prerequisites

- AWS account with EC2 and networking permissions
- AWS CLI installed ([Installation Guide](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html))
- SSH key pair in your AWS region

---

## Quick Start: Single EC2 Instance

**Best for:** Training sessions, workshops, small teams (up to 50 users)  
**Setup time:** ~30 minutes  
**Monthly cost:** $30-75

### Step 1: Launch EC2 Instance

In the AWS Console:
1. Go to **EC2 Dashboard** → **Launch Instance**
2. Configure:
   - **Name:** `security-shepherd`
   - **AMI:** Ubuntu Server 22.04 LTS
   - **Instance Type:** `t3.large` (adjust based on users)
   - **Key Pair:** Select existing or create new
   - **Security Group:** Allow ports 22 (SSH), 80 (HTTP), 443 (HTTPS)
   - **Storage:** 30 GB gp3
3. Launch instance

### Step 2: Install Dependencies

### Step 2: Install Dependencies

```bash
# Connect to instance
ssh -i your-key.pem ubuntu@<EC2-PUBLIC-IP>

# Install everything
sudo apt update && sudo apt install -y git maven docker.io docker-compose openjdk-11-jdk
sudo usermod -aG docker ubuntu

# Log out and back in
exit
ssh -i your-key.pem ubuntu@<EC2-PUBLIC-IP>
```

### Step 3: Deploy Security Shepherd

```bash
# Clone repository
git clone https://github.com/OWASP/SecurityShepherd.git
cd SecurityShepherd

# Build application (takes 10-15 minutes)
mvn -Pdocker clean install -DskipTests

# Start application
docker-compose up -d

# Verify containers are running
docker-compose ps
```

### Step 4: Access and Configure

1. Open browser: `http://<EC2-PUBLIC-IP>`
2. Login: `admin` / `password`
3. **Change password immediately**
4. Complete setup wizard

### Step 5: Enable HTTPS (Recommended)

**With a domain name:**

```bash
# Install Certbot
sudo snap install --classic certbot

# Stop Security Shepherd
cd ~/SecurityShepherd && docker-compose stop web

# Get SSL certificate
sudo certbot certonly --standalone -d your-domain.com

# Restart with SSL
docker-compose start web
```

**Without a domain:**  
Self-signed certificate is automatically generated. For production, consider using an Application Load Balancer with AWS Certificate Manager (see Production Deployment below).

### Quick Backup Setup

```bash
# Create backup script
cat > ~/backup.sh << 'EOF'
#!/bin/bash
BACKUP_DIR="/home/ubuntu/backups"
mkdir -p $BACKUP_DIR
DATE=$(date +%Y%m%d)

docker exec secshep_mariadb mysqldump -u root -pCowSaysMoo --all-databases > $BACKUP_DIR/db_$DATE.sql
docker exec secshep_mongo mongodump --out $BACKUP_DIR/mongo_$DATE

# Keep last 7 days
find $BACKUP_DIR -mtime +7 -delete
EOF

chmod +x ~/backup.sh

# Daily backups at 2 AM
(crontab -l 2>/dev/null; echo "0 2 * * * ~/backup.sh") | crontab -
```

---

## Production Deployment: ECS with Fargate

**Best for:** Production environments, high availability, auto-scaling  
**Setup time:** ~2 hours  
**Monthly cost:** $150-300+

This setup uses:
- **ECS Fargate** for container orchestration
- **RDS** for managed database
- **Application Load Balancer** with SSL
- **Auto-scaling** for high availability

### Prerequisites

Set up your AWS CLI with credentials:
```bash
aws configure
export AWS_REGION=us-east-1
export PROJECT_NAME=security-shepherd
```

### Step 1: Network Setup (5 minutes)

### Step 1: Network Setup (5 minutes)

```bash
# Set variables
export AWS_REGION=us-east-1
export PROJECT_NAME=security-shepherd

# Create VPC with subnets
### Step 1: Network Setup (5 minutes)

```bash
# Create VPC with subnets
VPC_ID=$(aws ec2 create-vpc --cidr-block 10.0.0.0/16 \
  --tag-specifications "ResourceType=vpc,Tags=[{Key=Name,Value=$PROJECT_NAME-vpc}]" \
  --query 'Vpc.VpcId' --output text)

aws ec2 modify-vpc-attribute --vpc-id $VPC_ID --enable-dns-hostnames

# Create Internet Gateway
IGW_ID=$(aws ec2 create-internet-gateway \
  --tag-specifications "ResourceType=internet-gateway,Tags=[{Key=Name,Value=$PROJECT_NAME-igw}]" \
  --query 'InternetGateway.InternetGatewayId' --output text)
aws ec2 attach-internet-gateway --vpc-id $VPC_ID --internet-gateway-id $IGW_ID

# Create subnets
PUBLIC_SUBNET_1=$(aws ec2 create-subnet --vpc-id $VPC_ID --cidr-block 10.0.1.0/24 \
  --availability-zone ${AWS_REGION}a --query 'Subnet.SubnetId' --output text)
PUBLIC_SUBNET_2=$(aws ec2 create-subnet --vpc-id $VPC_ID --cidr-block 10.0.2.0/24 \
  --availability-zone ${AWS_REGION}b --query 'Subnet.SubnetId' --output text)
PRIVATE_SUBNET_1=$(aws ec2 create-subnet --vpc-id $VPC_ID --cidr-block 10.0.11.0/24 \
  --availability-zone ${AWS_REGION}a --query 'Subnet.SubnetId' --output text)
PRIVATE_SUBNET_2=$(aws ec2 create-subnet --vpc-id $VPC_ID --cidr-block 10.0.12.0/24 \
  --availability-zone ${AWS_REGION}b --query 'Subnet.SubnetId' --output text)

# Configure routing
PUBLIC_RT=$(aws ec2 create-route-table --vpc-id $VPC_ID --query 'RouteTable.RouteTableId' --output text)
aws ec2 create-route --route-table-id $PUBLIC_RT --destination-cidr-block 0.0.0.0/0 --gateway-id $IGW_ID
aws ec2 associate-route-table --subnet-id $PUBLIC_SUBNET_1 --route-table-id $PUBLIC_RT
aws ec2 associate-route-table --subnet-id $PUBLIC_SUBNET_2 --route-table-id $PUBLIC_RT
```

### Step 2: Create RDS Database (10 minutes)

```bash
### Step 2: Create RDS Database (10 minutes)

```bash
# Create security groups
DB_SG=$(aws ec2 create-security-group --group-name $PROJECT_NAME-db-sg \
  --description "Security Shepherd database" --vpc-id $VPC_ID --query 'GroupId' --output text)
ECS_SG=$(aws ec2 create-security-group --group-name $PROJECT_NAME-ecs-sg \
  --description "Security Shepherd ECS" --vpc-id $VPC_ID --query 'GroupId' --output text)

aws ec2 authorize-security-group-ingress --group-id $DB_SG --protocol tcp --port 3306 --source-group $ECS_SG

# Create RDS
aws rds create-db-subnet-group --db-subnet-group-name $PROJECT_NAME-db-subnet \
  --db-subnet-group-description "Subnet group for Security Shepherd" \
  --subnet-ids $PRIVATE_SUBNET_1 $PRIVATE_SUBNET_2

aws rds create-db-instance \
  --db-instance-identifier ${PROJECT_NAME}-db \
  --db-instance-class db.t3.medium \
  --engine mariadb \
  --master-username root \
  --master-user-password 'YourStrongPassword123!' \
  --allocated-storage 20 \
  --db-subnet-group-name $PROJECT_NAME-db-subnet \
  --vpc-security-group-ids $DB_SG \
  --backup-retention-period 7 \
  --no-publicly-accessible

# Wait for RDS (takes 5-10 minutes)
aws rds wait db-instance-available --db-instance-identifier ${PROJECT_NAME}-db

DB_ENDPOINT=$(aws rds describe-db-instances --db-instance-identifier ${PROJECT_NAME}-db \
  --query 'DBInstances[0].Endpoint.Address' --output text)
echo "Database ready: $DB_ENDPOINT"
```

### Step 3: Build and Push to ECR (15 minutes)

```bash
### Step 3: Build and Push to ECR (15 minutes)

```bash
# Create ECR repositories
aws ecr create-repository --repository-name security-shepherd/web
aws ecr create-repository --repository-name security-shepherd/mongo

# Login to ECR
ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
aws ecr get-login-password --region $AWS_REGION | \
  docker login --username AWS --password-stdin ${ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com

# Build and push
git clone https://github.com/OWASP/SecurityShepherd.git
cd SecurityShepherd
mvn -Pdocker clean install -DskipTests
docker-compose build

ECR_REGISTRY="${ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
docker tag owasp/security-shepherd:latest $ECR_REGISTRY/security-shepherd/web:latest
docker push $ECR_REGISTRY/security-shepherd/web:latest
docker tag owasp/security-shepherd_mongo:latest $ECR_REGISTRY/security-shepherd/mongo:latest
docker push $ECR_REGISTRY/security-shepherd/mongo:latest
```

### Step 4: Create Load Balancer (5 minutes)

```bash
### Step 4: Create Load Balancer (5 minutes)

```bash
# Create ALB security group
ALB_SG=$(aws ec2 create-security-group --group-name $PROJECT_NAME-alb-sg \
  --description "ALB for Security Shepherd" --vpc-id $VPC_ID --query 'GroupId' --output text)

aws ec2 authorize-security-group-ingress --group-id $ALB_SG --protocol tcp --port 80 --cidr 0.0.0.0/0
aws ec2 authorize-security-group-ingress --group-id $ALB_SG --protocol tcp --port 443 --cidr 0.0.0.0/0
aws ec2 authorize-security-group-ingress --group-id $ECS_SG --protocol tcp --port 8080 --source-group $ALB_SG

# Create ALB
ALB_ARN=$(aws elbv2 create-load-balancer --name $PROJECT_NAME-alb \
  --subnets $PUBLIC_SUBNET_1 $PUBLIC_SUBNET_2 --security-groups $ALB_SG \
  --query 'LoadBalancers[0].LoadBalancerArn' --output text)

TG_ARN=$(aws elbv2 create-target-group --name $PROJECT_NAME-tg \
  --protocol HTTP --port 8080 --vpc-id $VPC_ID --target-type ip \
  --health-check-path / --query 'TargetGroups[0].TargetGroupArn' --output text)

aws elbv2 create-listener --load-balancer-arn $ALB_ARN --protocol HTTP --port 80 \
  --default-actions Type=forward,TargetGroupArn=$TG_ARN

ALB_DNS=$(aws elbv2 describe-load-balancers --load-balancer-arns $ALB_ARN \
  --query 'LoadBalancers[0].DNSName' --output text)
echo "Access at: http://$ALB_DNS"
```

### Step 5: Deploy to ECS (10 minutes)

```bash
### Step 5: Deploy to ECS (10 minutes)

```bash
# Create ECS cluster
aws ecs create-cluster --cluster-name $PROJECT_NAME-cluster

# Create IAM execution role
cat > task-role.json << 'EOF'
{
  "Version": "2012-10-17",
  "Statement": [{
    "Effect": "Allow",
    "Principal": {"Service": "ecs-tasks.amazonaws.com"},
    "Action": "sts:AssumeRole"
  }]
}
EOF

aws iam create-role --role-name ${PROJECT_NAME}-ecs-role \
  --assume-role-policy-document file://task-role.json

aws iam attach-role-policy --role-name ${PROJECT_NAME}-ecs-role \
  --policy-arn arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy

EXECUTION_ROLE_ARN=$(aws iam get-role --role-name ${PROJECT_NAME}-ecs-role \
  --query 'Role.Arn' --output text)

# Create task definition
cat > task.json << EOF
{
  "family": "$PROJECT_NAME",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "1024",
  "memory": "2048",
  "executionRoleArn": "$EXECUTION_ROLE_ARN",
  "containerDefinitions": [
    {
      "name": "shepherd-web",
      "image": "$ECR_REGISTRY/security-shepherd/web:latest",
      "essential": true,
      "portMappings": [{"containerPort": 8080}],
      "environment": [
        {"name": "MARIADB_URI", "value": "jdbc:mysql://$DB_ENDPOINT:3306"},
        {"name": "DB_USER", "value": "root"},
        {"name": "DB_PASS", "value": "YourStrongPassword123!"},
        {"name": "MONGO_HOST", "value": "localhost"},
        {"name": "MONGO_PORT", "value": "27017"}
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/$PROJECT_NAME",
          "awslogs-region": "$AWS_REGION",
          "awslogs-stream-prefix": "web",
          "awslogs-create-group": "true"
        }
      }
    },
    {
      "name": "shepherd-mongo",
      "image": "$ECR_REGISTRY/security-shepherd/mongo:latest",
      "essential": false,
      "portMappings": [{"containerPort": 27017}],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/$PROJECT_NAME",
          "awslogs-region": "$AWS_REGION",
          "awslogs-stream-prefix": "mongo",
          "awslogs-create-group": "true"
        }
      }
    }
  ]
}
EOF

aws ecs register-task-definition --cli-input-json file://task.json

# Create ECS service
aws ecs create-service \
  --cluster $PROJECT_NAME-cluster \
  --service-name $PROJECT_NAME-service \
  --task-definition $PROJECT_NAME \
  --desired-count 2 \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={subnets=[$PRIVATE_SUBNET_1,$PRIVATE_SUBNET_2],securityGroups=[$ECS_SG]}" \
  --load-balancers "targetGroupArn=$TG_ARN,containerName=shepherd-web,containerPort=8080" \
  --health-check-grace-period-seconds 300

echo "✅ Deployment complete! Access at: http://$ALB_DNS"
echo "Initial login: admin / password (change immediately)"
```

### Optional: Add HTTPS with SSL

If you have a domain, add SSL:

```bash
# Request ACM certificate
CERT_ARN=$(aws acm request-certificate --domain-name your-domain.com \
  --validation-method DNS --query 'CertificateArn' --output text)

# After DNS validation, add HTTPS listener
aws elbv2 create-listener --load-balancer-arn $ALB_ARN \
  --protocol HTTPS --port 443 --certificates CertificateArn=$CERT_ARN \
  --default-actions Type=forward,TargetGroupArn=$TG_ARN
```

---

## Security & Maintenance

### Essential Security Steps

1. **Change All Default Passwords**
   - Admin password (via web interface)
   - Database passwords (RDS and environment variables)

2. **Restrict Security Groups**
   - Limit SSH to your IP only
   - Keep databases in private subnets
   - Only allow HTTPS in production

3. **Enable Automated Backups**
   - RDS: Automated daily backups (already enabled, 7-day retention)
   - EC2: Set up EBS snapshots via Data Lifecycle Manager

4. **Use Secrets Manager** (recommended for production)
```bash
aws secretsmanager create-secret --name $PROJECT_NAME/db-password \
  --secret-string "YourStrongPassword123!"
```

### Monitoring

**Key metrics to watch:**
- CPU/Memory utilization (should stay below 80%)
- Database connections
- ALB 5xx errors
- Disk space

**Set up CloudWatch alarms:**
```bash
aws cloudwatch put-metric-alarm --alarm-name ${PROJECT_NAME}-high-cpu \
  --metric-name CPUUtilization --namespace AWS/ECS \
  --statistic Average --period 300 --threshold 80 \
  --comparison-operator GreaterThanThreshold --evaluation-periods 2
```

### Backup Strategy

**EC2 Deployment:**
- Use the backup script from Quick Start section
- Schedule EBS snapshots weekly

**ECS Deployment:**
- RDS automated backups (enabled by default)
- Manual snapshots before updates:
```bash
aws rds create-db-snapshot --db-instance-identifier ${PROJECT_NAME}-db \
  --db-snapshot-identifier ${PROJECT_NAME}-backup-$(date +%Y%m%d)
```

### Cost Optimization

**Estimated monthly costs:**
- EC2 t3.large: $60-75
- ECS Fargate (2 tasks) + RDS: $200-300

**Save money:**
- Use Savings Plans for 30-60% off long-term deployments
- Scale down non-production environments during off-hours
- Use gp3 instead of gp2 storage
- Review AWS Trusted Advisor recommendations

---

## Troubleshooting

### Can't Access Security Shepherd

```bash
# Check if service is running
# EC2:
docker-compose ps

# ECS:
aws ecs describe-services --cluster $PROJECT_NAME-cluster \
  --services $PROJECT_NAME-service

# Check ALB target health
aws elbv2 describe-target-health --target-group-arn $TG_ARN

# Check security groups allow traffic
aws ec2 describe-security-groups --group-ids <SG-ID>
```

### Database Connection Errors

```bash
# EC2: Check database container
docker logs secshep_mariadb

# ECS: Verify RDS endpoint and credentials
aws rds describe-db-instances --db-instance-identifier ${PROJECT_NAME}-db

# Test connection from ECS task
# Update task definition with correct DB_ENDPOINT and DB_PASS
```

### Container Won't Start

```bash
# Check ECS logs
aws logs tail /ecs/$PROJECT_NAME --follow

# Check task definition
aws ecs describe-task-definition --task-definition $PROJECT_NAME

# Verify ECR images
aws ecr describe-images --repository-name security-shepherd/web
```

### High Memory Usage

```bash
# EC2: Check container resources
docker stats

# ECS: Increase memory in task definition
# Edit task.json and change "memory": "2048" to "4096"
# Then re-register and update service
```

### View Application Logs

```bash
# EC2:
docker logs -f secshep_tomcat

# ECS:
aws logs tail /ecs/$PROJECT_NAME/web --follow
```

---

## Additional Resources

- [Security Shepherd Wiki](https://github.com/OWASP/SecurityShepherd/wiki)
- [Docker Setup Guide](https://github.com/OWASP/SecurityShepherd/wiki/Docker-Environment-Setup)
- [AWS ECS Documentation](https://docs.aws.amazon.com/ecs/)
- [AWS RDS Best Practices](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/CHAP_BestPractices.html)

---

**Need Help?**
- [GitHub Issues](https://github.com/OWASP/SecurityShepherd/issues)
- [OWASP Slack](https://owasp.org/slack/invite) - #security-shepherd channel

**Last Updated:** February 2026
