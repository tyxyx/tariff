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

# Latest Ubuntu 22.04 LTS
data "aws_ami" "ubuntu" {
  most_recent = true
  owners      = ["099720109477"] # Canonical
  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd-gp3/ubuntu-noble-24.04-amd64-server-*"]
  }
}

resource "aws_security_group" "backend_sg" {
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

# User data installs Docker, Docker Compose and runs the application
locals {
  # Read the original compose.yaml file
  
  user_data = <<-EOF
    #!/bin/bash
    set -eux

    # Update package index
    sudo apt-get update -y
    
    # Install Docker
    sudo apt-get install -y ca-certificates curl gnupg lsb-release
    sudo mkdir -p /etc/apt/keyrings
    sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
    sudo apt-get update -y
    sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
    
    # Start Docker and enable it to start on boot
    sudo systemctl enable --now docker
    sudo usermod -aG docker ubuntu
    sudo docker pull ${var.image_name}:${var.image_tag}
    
    # Start the application using Docker Compose
    cd /home/ubuntu
    sudo docker compose -f compose.yaml up -d
  EOF
}
data "aws_eip" "existing" {
  public_ip = var.elastic_ip  # Or use id = var.elastic_ip_id if you have the allocation ID
}

resource "aws_instance" "backend" {
  ami                         = data.aws_ami.ubuntu.id
  instance_type               = var.instance_type
  subnet_id                   = data.aws_subnets.default.ids[0]
  vpc_security_group_ids      = [aws_security_group.backend_sg.id]
  associate_public_ip_address = true
  key_name                    = var.key_name
  user_data                   = local.user_data
  tags = { Name = var.name }

  provisioner "file" {
    source      = var.compose_file_path
    destination = "/home/ubuntu/compose.yaml"
    connection {
      type        = "ssh"
      user        = "ubuntu" # Or appropriate user for your AMI
      private_key = file(var.ssh_key_path) # Path to your private key
      host        = self.public_ip # Or self.private_ip if accessing privately
    }
  }
  provisioner "file" {
    source      = var.env_file_path
    destination = "/home/ubuntu/.env"
    connection {
      type        = "ssh"
      user        = "ubuntu" # Or appropriate user for your AMI
      private_key = file(var.ssh_key_path) # Path to your private key
      host        = self.public_ip # Or self.private_ip if accessing privately
    }
  }
  
}
resource "aws_eip_association" "backend_eip_assoc" {
  instance_id   = aws_instance.backend.id
  allocation_id = data.aws_eip.existing.id
}

output "public_ip"   { value = var.elastic_ip }
output "backend_url" { value = "http://${var.elastic_ip}:${var.host_port}" }
