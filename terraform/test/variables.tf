variable "name" {
	type    = string
	default = "springboot-ec2"
}
variable "aws_region" {
	type    = string
	default = "ap-southeast-1"
}
variable "instance_type" {
	type    = string
	default = "t3.micro"
}
variable "ecr_repo_name" {
	type = string
}
variable "account_id" {
	type = string
}
variable "ssh_public_key" {
	type = string
}
variable "allow_ssh_cidr" {
	type    = list(string)
	default = ["0.0.0.0/0"]
}

# App env
variable "spring_profile" {
	type    = string
	default = "prod"
}
variable "db_url" {
	type    = string
	default = ""
}
variable "db_username" {
	type    = string
	default = ""
}
variable "db_password" {
	type    = string
	default = ""
}
