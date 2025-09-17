data "aws_vpc" "main" {
    provider = aws
    id = "vpc-089e9760c65e974ea"
}

output "vpc_cidr"{
    value = data.aws_vpc.main.cidr_block

}