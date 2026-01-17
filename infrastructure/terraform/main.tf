# =============================================================================
# Terraform Configuration for Microservices Architecture
# 2 EC2 Instances + 1 RDS PostgreSQL (us-east-1)
# =============================================================================

terraform {
  required_version = ">= 1.5.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

# =============================================================================
# Provider Configuration
# =============================================================================
provider "aws" {
  region = var.aws_region
}

# =============================================================================
# Variables
# =============================================================================
variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "us-east-1"
}

variable "db_username" {
  description = "Database administrator username"
  type        = string
  default     = "dbadmin"
  sensitive   = true
}

variable "db_password" {
  description = "Database administrator password"
  type        = string
  default     = "SecurePassword123!"
  sensitive   = true
}

variable "key_name" {
  description = "EC2 Key Pair name for SSH access"
  type        = string
  default     = "microservices-keypair"
}

variable "environment" {
  description = "Environment name"
  type        = string
  default     = "production"
}

# =============================================================================
# Data Sources
# =============================================================================
data "aws_availability_zones" "available" {
  state = "available"
}

data "aws_ami" "amazon_linux_2023" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-2023.*-x86_64"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

# =============================================================================
# VPC and Networking
# =============================================================================
resource "aws_vpc" "main" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = {
    Name        = "microservices-vpc"
    Environment = var.environment
  }
}

resource "aws_internet_gateway" "main" {
  vpc_id = aws_vpc.main.id

  tags = {
    Name        = "microservices-igw"
    Environment = var.environment
  }
}

# Public Subnets
resource "aws_subnet" "public_a" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = "10.0.1.0/24"
  availability_zone       = data.aws_availability_zones.available.names[0]
  map_public_ip_on_launch = true

  tags = {
    Name        = "public-subnet-a"
    Environment = var.environment
  }
}

resource "aws_subnet" "public_b" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = "10.0.2.0/24"
  availability_zone       = data.aws_availability_zones.available.names[1]
  map_public_ip_on_launch = true

  tags = {
    Name        = "public-subnet-b"
    Environment = var.environment
  }
}

# Private Subnets for RDS
resource "aws_subnet" "private_a" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = "10.0.10.0/24"
  availability_zone = data.aws_availability_zones.available.names[0]

  tags = {
    Name        = "private-subnet-a"
    Environment = var.environment
  }
}

resource "aws_subnet" "private_b" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = "10.0.11.0/24"
  availability_zone = data.aws_availability_zones.available.names[1]

  tags = {
    Name        = "private-subnet-b"
    Environment = var.environment
  }
}

# Route Table for Public Subnets
resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.main.id
  }

  tags = {
    Name        = "public-route-table"
    Environment = var.environment
  }
}

resource "aws_route_table_association" "public_a" {
  subnet_id      = aws_subnet.public_a.id
  route_table_id = aws_route_table.public.id
}

resource "aws_route_table_association" "public_b" {
  subnet_id      = aws_subnet.public_b.id
  route_table_id = aws_route_table.public.id
}

# =============================================================================
# Security Groups
# =============================================================================

# Security Group for ShopServer (Server A) - External Access on Port 8080
resource "aws_security_group" "shop_server_sg" {
  name        = "shop-server-sg"
  description = "Security group for Shop Management Server (Server A)"
  vpc_id      = aws_vpc.main.id

  # Allow external traffic on port 8080 (Shop Management API)
  ingress {
    description = "Allow external HTTP traffic to Shop API"
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # Allow SSH access (restrict to your IP in production)
  ingress {
    description = "Allow SSH access"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # Allow all outbound traffic
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name        = "ShopServer-SG"
    Environment = var.environment
    Application = "shop-management"
  }
}

# Security Group for StockServer (Server B) - Internal Access Only
resource "aws_security_group" "stock_server_sg" {
  name        = "stock-server-sg"
  description = "Security group for Product Stock Server (Server B)"
  vpc_id      = aws_vpc.main.id

  # Allow traffic on port 8081 ONLY from ShopServer SG (internal communication)
  ingress {
    description     = "Allow traffic from ShopServer on port 8081"
    from_port       = 8081
    to_port         = 8081
    protocol        = "tcp"
    security_groups = [aws_security_group.shop_server_sg.id]
  }

  # Allow SSH access (restrict to your IP in production)
  ingress {
    description = "Allow SSH access"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # Allow all outbound traffic
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name        = "StockServer-SG"
    Environment = var.environment
    Application = "product-stock"
  }
}

# Security Group for RDS - Access from Both EC2 Instances
resource "aws_security_group" "rds_sg" {
  name        = "rds-sg"
  description = "Security group for RDS PostgreSQL"
  vpc_id      = aws_vpc.main.id

  # Allow PostgreSQL traffic from ShopServer
  ingress {
    description     = "Allow PostgreSQL from ShopServer"
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.shop_server_sg.id]
  }

  # Allow PostgreSQL traffic from StockServer
  ingress {
    description     = "Allow PostgreSQL from StockServer"
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.stock_server_sg.id]
  }

  # Allow all outbound traffic
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name        = "RDS-SG"
    Environment = var.environment
  }
}

# =============================================================================
# RDS PostgreSQL Instance
# =============================================================================
resource "aws_db_subnet_group" "main" {
  name       = "microservices-db-subnet-group"
  subnet_ids = [aws_subnet.private_a.id, aws_subnet.private_b.id]

  tags = {
    Name        = "microservices-db-subnet-group"
    Environment = var.environment
  }
}

resource "aws_db_instance" "main" {
  identifier = "microservices-db"

  # Engine Configuration
  engine               = "postgres"
  engine_version       = "15.4"
  instance_class       = "db.t3.micro"
  
  # Storage Configuration - Max 50GB
  allocated_storage     = 20
  max_allocated_storage = 50
  storage_type          = "gp3"
  storage_encrypted     = true

  # Database Configuration
  db_name  = "microservices"
  username = var.db_username
  password = var.db_password
  port     = 5432

  # Network Configuration
  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = [aws_security_group.rds_sg.id]
  publicly_accessible    = false
  multi_az               = false

  # Backup Configuration
  backup_retention_period = 7
  backup_window           = "03:00-04:00"
  maintenance_window      = "Mon:04:00-Mon:05:00"

  # Performance and Monitoring
  performance_insights_enabled = false
  monitoring_interval          = 0

  # Other Settings
  skip_final_snapshot       = true
  final_snapshot_identifier = "microservices-db-final-snapshot"
  deletion_protection       = false
  auto_minor_version_upgrade = true

  tags = {
    Name        = "microservices-db"
    Environment = var.environment
  }
}

# =============================================================================
# IAM Role for EC2 Instances
# =============================================================================
resource "aws_iam_role" "ec2_role" {
  name = "microservices-ec2-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ec2.amazonaws.com"
        }
      }
    ]
  })

  tags = {
    Name        = "microservices-ec2-role"
    Environment = var.environment
  }
}

resource "aws_iam_role_policy_attachment" "ssm_policy" {
  role       = aws_iam_role.ec2_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_instance_profile" "ec2_profile" {
  name = "microservices-ec2-profile"
  role = aws_iam_role.ec2_role.name
}

# =============================================================================
# EC2 Instance 1 - ShopServer (Server A)
# =============================================================================
resource "aws_instance" "shop_server" {
  ami                    = data.aws_ami.amazon_linux_2023.id
  instance_type          = "t3.small"
  key_name               = var.key_name
  subnet_id              = aws_subnet.public_a.id
  vpc_security_group_ids = [aws_security_group.shop_server_sg.id]
  iam_instance_profile   = aws_iam_instance_profile.ec2_profile.name

  root_block_device {
    volume_size           = 20
    volume_type           = "gp3"
    encrypted             = true
    delete_on_termination = true
  }

  user_data = base64encode(<<-EOF
    #!/bin/bash
    set -e
    
    # Update system
    dnf update -y
    
    # Install Java 21 (Amazon Corretto)
    dnf install -y java-21-amazon-corretto-devel
    
    # Install Maven
    dnf install -y maven
    
    # Install Git
    dnf install -y git
    
    # Create application directory
    mkdir -p /opt/microservices/shop-management
    
    # Create log directory with proper permissions
    mkdir -p /var/log/shop-app
    chmod 755 /var/log/shop-app
    chown ec2-user:ec2-user /var/log/shop-app
    
    # Set environment variables
    echo "export JAVA_HOME=/usr/lib/jvm/java-21-amazon-corretto" >> /etc/profile.d/java.sh
    echo "export PATH=\$PATH:\$JAVA_HOME/bin" >> /etc/profile.d/java.sh
    source /etc/profile.d/java.sh
    
    # Verify installations
    java -version
    mvn -version
    git --version
    
    echo "ShopServer setup completed successfully!"
  EOF
  )

  tags = {
    Name        = "ShopServer"
    Environment = var.environment
    Application = "shop-management"
    Role        = "Orchestrator"
  }
}

# =============================================================================
# EC2 Instance 2 - StockServer (Server B)
# =============================================================================
resource "aws_instance" "stock_server" {
  ami                    = data.aws_ami.amazon_linux_2023.id
  instance_type          = "t3.small"
  key_name               = var.key_name
  subnet_id              = aws_subnet.public_a.id
  vpc_security_group_ids = [aws_security_group.stock_server_sg.id]
  iam_instance_profile   = aws_iam_instance_profile.ec2_profile.name

  root_block_device {
    volume_size           = 20
    volume_type           = "gp3"
    encrypted             = true
    delete_on_termination = true
  }

  user_data = base64encode(<<-EOF
    #!/bin/bash
    set -e
    
    # Update system
    dnf update -y
    
    # Install Java 21 (Amazon Corretto)
    dnf install -y java-21-amazon-corretto-devel
    
    # Install Maven
    dnf install -y maven
    
    # Install Git
    dnf install -y git
    
    # Create application directory
    mkdir -p /opt/microservices/product-stock
    
    # Create log directory with proper permissions
    mkdir -p /var/log/stock-app
    chmod 755 /var/log/stock-app
    chown ec2-user:ec2-user /var/log/stock-app
    
    # Set environment variables
    echo "export JAVA_HOME=/usr/lib/jvm/java-21-amazon-corretto" >> /etc/profile.d/java.sh
    echo "export PATH=\$PATH:\$JAVA_HOME/bin" >> /etc/profile.d/java.sh
    source /etc/profile.d/java.sh
    
    # Verify installations
    java -version
    mvn -version
    git --version
    
    echo "StockServer setup completed successfully!"
  EOF
  )

  tags = {
    Name        = "StockServer"
    Environment = var.environment
    Application = "product-stock"
    Role        = "Backend"
  }
}

# =============================================================================
# Outputs
# =============================================================================
output "vpc_id" {
  description = "VPC ID"
  value       = aws_vpc.main.id
}

output "rds_endpoint" {
  description = "RDS PostgreSQL endpoint"
  value       = aws_db_instance.main.endpoint
}

output "rds_address" {
  description = "RDS PostgreSQL address (hostname only)"
  value       = aws_db_instance.main.address
}

output "rds_port" {
  description = "RDS PostgreSQL port"
  value       = aws_db_instance.main.port
}

output "shop_server_public_ip" {
  description = "Public IP of Shop Management Server (Server A)"
  value       = aws_instance.shop_server.public_ip
}

output "shop_server_private_ip" {
  description = "Private IP of Shop Management Server (Server A)"
  value       = aws_instance.shop_server.private_ip
}

output "stock_server_public_ip" {
  description = "Public IP of Product Stock Server (Server B)"
  value       = aws_instance.stock_server.public_ip
}

output "stock_server_private_ip" {
  description = "Private IP of Product Stock Server (Server B)"
  value       = aws_instance.stock_server.private_ip
}

output "shop_api_url" {
  description = "Shop Management API URL"
  value       = "http://${aws_instance.shop_server.public_ip}:8080/api/shop"
}

output "stock_internal_url" {
  description = "Product Stock Internal URL (for Server A to call)"
  value       = "http://${aws_instance.stock_server.private_ip}:8081"
}

output "connection_instructions" {
  description = "Instructions for connecting services"
  value       = <<-EOT
    ================================================================================
    DEPLOYMENT INSTRUCTIONS
    ================================================================================
    
    1. SSH into ShopServer:
       ssh -i ${var.key_name}.pem ec2-user@${aws_instance.shop_server.public_ip}
    
    2. SSH into StockServer:
       ssh -i ${var.key_name}.pem ec2-user@${aws_instance.stock_server.public_ip}
    
    3. Set environment variables on ShopServer:
       export RDS_ENDPOINT=${aws_db_instance.main.address}
       export DB_USERNAME=${var.db_username}
       export DB_PASSWORD=<your-password>
       export STOCK_SERVICE_HOST=${aws_instance.stock_server.private_ip}
       export STOCK_SERVICE_PORT=8081
    
    4. Set environment variables on StockServer:
       export RDS_ENDPOINT=${aws_db_instance.main.address}
       export DB_USERNAME=${var.db_username}
       export DB_PASSWORD=<your-password>
    
    5. Create database schemas:
       psql -h ${aws_db_instance.main.address} -U ${var.db_username} -d microservices
       CREATE SCHEMA IF NOT EXISTS shop_db;
       CREATE SCHEMA IF NOT EXISTS stock_db;
    
    ================================================================================
  EOT
}
