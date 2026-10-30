resource "aws_vpc" "workshop" {
  cidr_block           = "10.42.0.0/16"
  enable_dns_hostnames = true
  enable_dns_support   = true
}
resource "aws_internet_gateway" "workshop" { vpc_id = aws_vpc.workshop.id }
resource "aws_subnet" "public" {
  count                   = 2
  vpc_id                  = aws_vpc.workshop.id
  availability_zone       = data.aws_availability_zones.available.names[count.index]
  cidr_block              = cidrsubnet(aws_vpc.workshop.cidr_block, 8, count.index)
  map_public_ip_on_launch = true
  tags = {
    Name                     = "${var.name}-public-${count.index}"
    "kubernetes.io/role/elb" = "1"
  }
}
resource "aws_route_table" "public" {
  vpc_id = aws_vpc.workshop.id
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.workshop.id
  }
}
resource "aws_route_table_association" "public" {
  count          = 2
  subnet_id      = aws_subnet.public[count.index].id
  route_table_id = aws_route_table.public.id
}
# No NAT Gateway and no SSH ingress. Managed nodes share the EKS security group.
# The ALB controller manages its front-end and target security group rules.
