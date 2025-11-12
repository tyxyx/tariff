terraform {
  required_version = ">= 1.6.0"
  required_providers {
    aws = { source = "hashicorp/aws", version = "~> 5.0" }
  }
}

provider "aws" {
  region  = var.region
  profile = "terraform-admin"
}

# Use default VPC/subnet(s) to keep this minimal
data "aws_vpc" "default" { default = true }
data "aws_subnets" "default" {
  filter {
    name   = "vpc-id"
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

resource "aws_key_pair" "key_pair" {
  key_name   = "${var.name}KeyPair"
  public_key = file(var.public_key_path) # Path to your public key file
}

resource "aws_security_group" "scrapper_sg" {
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

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_instance" "scrapper" {
  ami                         = data.aws_ami.ubuntu.id
  instance_type               = var.instance_type
  subnet_id                   = data.aws_subnets.default.ids[0]
  vpc_security_group_ids      = [aws_security_group.scrapper_sg.id]
  associate_public_ip_address = true
  key_name                    = aws_key_pair.key_pair.key_name
  tags                        = { Name = var.name }

  provisioner "file" {
    source      = var.env_file_path
    destination = "/home/ubuntu/.env"
    connection {
      type        = "ssh"
      user        = "ubuntu"
      private_key = file(var.private_key_path) # Path to your private key
      host        = self.public_ip
    }
  }

  provisioner "file" {
    source      = var.requirements_file_path
    destination = "/home/ubuntu/requirements.txt"
    connection {
      type        = "ssh"
      user        = "ubuntu"
      private_key = file(var.private_key_path)
      host        = self.public_ip
    }
  }

  provisioner "file" {
    source      = var.scrapper_file_path
    destination = "/home/ubuntu/scrapper.py"
    connection {
      type        = "ssh"
      user        = "ubuntu"
      private_key = file(var.private_key_path)
      host        = self.public_ip
    }
  }

  # Run a script after files are copied
  provisioner "remote-exec" {
  inline = [
    "sudo apt-get update -y",
    "sudo apt-get install -y python3.12 python3.12-venv python3-pip",  # Use python3-pip instead
    "chmod +x /home/ubuntu/scrapper.py",
    "python3.12 -m venv /home/ubuntu/.venv",  # Create venv with python3.12
    "chmod +x /home/ubuntu/.venv/bin/pip",
    "/home/ubuntu/.venv/bin/pip install --upgrade pip",
    "/home/ubuntu/.venv/bin/pip install -r /home/ubuntu/requirements.txt"
  ]
  connection {
    type        = "ssh"
    user        = "ubuntu"
    private_key = file(var.private_key_path)
    host        = self.public_ip
  }
}
}

resource "aws_eip" "scrapper_eip" {}

resource "aws_eip_association" "scrapper_eip_assoc" {
  instance_id   = aws_instance.scrapper.id
  allocation_id = aws_eip.scrapper_eip.id
}

output "private_key_path" {
  value = var.private_key_path
}

output "elastic_ip" {
  value = aws_eip.scrapper_eip.public_ip
}

output "ssh_command" {
  value = "ssh -i ${var.private_key_path} ubuntu@${aws_eip.scrapper_eip.public_ip}"
}