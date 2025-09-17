output "public_ip" {
  value = aws_instance.api.public_ip
}

output "http_url" {
  value = "http://${aws_instance.api.public_dns}"
}