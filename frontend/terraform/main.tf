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


resource "aws_security_group" "frontend_sg" {
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

# User data pulls and runs your frontend container
locals {
  user_data = <<-EOF
    #!/bin/bash
    set -eux

    dnf update -y
    dnf install -y docker
    systemctl enable --now docker
    usermod -aG docker ec2-user

    # Pull & run
    docker pull ${var.image}
    docker stop ${var.name} || true
    docker rm ${var.name} || true

    # Run Next.js frontend container
    docker run -d --name ${var.name} -p ${var.host_port}:${var.container_port} \
      --restart unless-stopped \
      ${join(" ", [for k,v in var.env : "--env \"${k}=${v}\""])} \
      ${var.image}
  EOF
}

resource "aws_instance" "frontend" {
  ami                         = data.aws_ami.al2023.id
  instance_type               = var.instance_type
  subnet_id                   = data.aws_subnets.default.ids[0]
  vpc_security_group_ids      = [aws_security_group.frontend_sg.id]
  associate_public_ip_address = true
  key_name                    = var.key_name
  user_data                   = local.user_data
  tags = { Name = var.name }
}

data "aws_eip" "existing" {
  public_ip = var.elastic_ip  # Or use id = var.elastic_ip_id if you have the allocation ID
}

resource "aws_eip_association" "app_eip_assoc" {
  instance_id   = aws_instance.frontend.id
  allocation_id = data.aws_eip.existing.id
}


output "public_ip"   { value = aws_instance.frontend.public_ip }
output "frontend_url" { value = "http://${data.aws_eip.existing.public_ip}:${var.host_port}" }