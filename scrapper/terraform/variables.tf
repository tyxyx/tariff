variable "region" {
  type    = string
  default = "ap-southeast-1"
}

variable "name" {
  type    = string
  default = "scrapper"
}

variable "instance_type" {
  type    = string
  default = "t3.micro"
}

variable "public_key_path" {
  type    = string
  default = "../../scrapperKeyPair.pub" # Path to your public key file
}

variable "private_key_path" {
  type    = string
  default = "../../scrapperKeyPair.pem" # Path to your private key file
}

variable "requirements_file_path" {
  type    = string
  default = "../requirements.txt" # Path to the Python requirements file
}

variable "env_file_path" {
  type    = string
  default = "../.env" # Path to the environment variables file
}

variable "scrapper_file_path" {
  type    = string
  default = "../scrapper.py" # Path to the environment variables file
}