variable "region"         {
  type = string
  default = "ap-southeast-1"
}
variable "name"           {
  type = string
  default = "springboot-app"
}
variable "instance_type"  {
  type = string
  default = "t3.micro"
}
variable "key_name"       {
  type = string
  default = "backEndKeyPair"
} # existing EC2 key pair
variable "ssh_cidrs"      {
  type = list(string)
  default = ["0.0.0.0/0"]
}

# Container config
variable "image_name"          {
    type = string
    default = "docker.io/wzinl/tariffbackend" 
}

variable "image_tag"          {
    type = string
    default = "latest"
}

variable "container_port" {
    type = number
    default = 8080
}

variable "host_port"      {
    type = number
    default = 8080
}
variable "env"            {
    type = map(string)
    default = {}
}

variable "elastic_ip"          {
    type = string
    default = "18.139.89.63"
}

variable "ssh_key_path" {
    type = string
    default = "../../backEndKeyPair.pem"
}

variable "compose_file_path" {
    type = string
    default = "../compose.yaml"
}

variable "env_file_path" {
    type = string
    default = "../.env"
}