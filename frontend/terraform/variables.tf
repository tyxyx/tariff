variable "region" {
  type    = string
  default = "ap-southeast-1"
}

variable "name" {
  type    = string
  default = "nextjs-frontend"
}

variable "instance_type" {
  type    = string
  default = "t3.micro"
}

variable "key_name" {
  type    = string
  default = "frontEndKeyPair" 
}

variable "ssh_cidrs" {
  type    = list(string)
  default = ["0.0.0.0/0"]
}

# Container config
variable "image_name" {
  type    = string
  default = "docker.io/wzinl/tarifffrontend" # Replace with your Docker Hub image
}

variable "image_tag" {
  type    = string
  default = "latest"
}

variable "container_port" {
  type    = number
  default = 3000 # Next.js default port
}

variable "host_port" {
  type    = number
  default = 3000
}

variable "env" {
  type    = map(string)
  default = {
    NODE_ENV = "production"
    # Add any other environment variables your Next.js app needs
  }
}

variable "ssh_key_path" {
    type = string
    default = "../../frontEndKeyPair.pem"
}

variable "compose_file_path" {
    type = string
    default = "../compose.yaml"
}

variable "env_file_path" {
    type = string
    default = "../.env.local"
}

variable "elastic_ip" {
    type = string
    default = "47.129.81.160"
}