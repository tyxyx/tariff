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
  default = "springbootKeypair"
} # existing EC2 key pair
variable "ssh_cidrs"      {
  type = list(string)
  default = ["0.0.0.0/0"]
}

# Container config
variable "image"          {
    type = string
    default = "docker.io/wzinl/tariffbackend:latest" # Replace with your image
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
