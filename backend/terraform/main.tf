terraform {
  required_version = ">= 1.6.0"
  required_providers {
    aws = { source = "hashicorp/aws", version = "~> 5.0" }
  }
}

provider "aws" {
  region = var.region
  profile = "terraform-admin"
}

# Use default VPC/subnet(s) to keep this minimal
data "aws_vpc" "default" { default = true }
data "aws_subnets" "default" { 
  filter {
    name = "vpc-id"
    values = [data.aws_vpc.default.id]
  }
}

# Latest Amazon Linux 2023
data "aws_ami" "al2023" {
  most_recent = true
  owners      = ["137112412989"] # Amazon
  filter {
    name   = "name"
    values = ["al2023-ami-*-x86_64"]
  }
}

resource "aws_security_group" "app_sg" {
  name        = "${var.name}-sg"
  description = "Allow SSH and app traffic"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    description = "SSH"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  ingress {
    description = "App"
    from_port   = var.host_port
    to_port     = var.host_port
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# (Optional) IAM role to read from ECR. Keep if using ECR; safe otherwise.
resource "aws_iam_role" "ec2_role" {
  name               = "${var.name}-role"
  assume_role_policy = jsonencode({ Version = "2012-10-17", Statement = [{ Effect="Allow", Principal={Service="ec2.amazonaws.com"}, Action="sts:AssumeRole" }] })
}

resource "aws_iam_role_policy_attachment" "ecr_read" {
  role       = aws_iam_role.ec2_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
}
resource "aws_iam_instance_profile" "ec2_profile" {
  name = "${var.name}-profile"
  role = aws_iam_role.ec2_role.name
}

# User data pulls and runs your container
locals {
  user_data = <<-EOF
    #!/bin/bash
    set -eux

    dnf update -y
    dnf install -y docker
    systemctl enable --now docker
    usermod -aG docker ec2-user

    # If using ECR, uncomment these 2 lines and set var.image to your ECR image URI
    # REGION="${var.region}"
    # aws ecr get-login-password --region $REGION | docker login --username AWS --password-stdin $(echo ${var.image} | cut -d'/' -f1)

    # Pull & run
    docker pull ${var.image}
    docker stop ${var.name} || true
    docker rm ${var.name} || true

    # Example: expose ${var.host_port}->${var.container_port} and pass envs
    docker run -d --name ${var.name} -p ${var.host_port}:${var.container_port} \
      --restart unless-stopped \
      ${join(" ", [for k,v in var.env : "--env \"${k}=${v}\""])} \
      ${var.image}
  EOF
}

resource "aws_instance" "app" {
  ami                         = data.aws_ami.al2023.id
  instance_type               = var.instance_type
  subnet_id                   = data.aws_subnets.default.ids[0]
  vpc_security_group_ids      = [aws_security_group.app_sg.id]
  associate_public_ip_address = true
  # key_name = var.key_name
  iam_instance_profile        = aws_iam_instance_profile.ec2_profile.name
  user_data                   = local.user_data

  tags = { Name = var.name }
}

output "public_ip"   { value = aws_instance.app.public_ip }
output "app_url"     { value = "http://${aws_instance.app.public_ip}:${var.host_port}" }
