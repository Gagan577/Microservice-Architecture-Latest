# Microservices Architecture - Shop Management & Product Stock

## Overview
Production-grade microservices solution with two Spring Boot applications deployed on separate AWS EC2 instances, demonstrating REST, SOAP, and GraphQL communication patterns.

## Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              AWS Cloud (us-east-1)                          │
│                                                                             │
│  ┌────────────────────────────────────────────────────────────────────────┐ │
│  │                              VPC (10.0.0.0/16)                          │ │
│  │                                                                         │ │
│  │  ┌─────────────────────────┐       ┌─────────────────────────┐        │ │
│  │  │   Public Subnet         │       │   Private Subnet         │        │ │
│  │  │   10.0.1.0/24           │       │   10.0.2.0/24            │        │ │
│  │  │                         │       │                          │        │ │
│  │  │  ┌───────────────────┐  │       │  ┌───────────────────┐   │        │ │
│  │  │  │ Server A (EC2)    │  │       │  │ Server B (EC2)    │   │        │ │
│  │  │  │ shop-management   │  │  ────►│  │ product-stock     │   │        │ │
│  │  │  │ Port 8080 (Public)│  │ 8081  │  │ Port 8081         │   │        │ │
│  │  │  └───────────────────┘  │       │  └───────────────────┘   │        │ │
│  │  │                         │       │                          │        │ │
│  │  └─────────────────────────┘       └─────────────────────────┘        │ │
│  │                                              │                         │ │
│  │                                              ▼                         │ │
│  │                              ┌───────────────────────────────┐        │ │
│  │                              │      RDS PostgreSQL           │        │ │
│  │                              │      db.t3.micro (50GB)       │        │ │
│  │                              │  ┌─────────┬─────────┐        │        │ │
│  │                              │  │ shop_db │stock_db │        │        │ │
│  │                              │  └─────────┴─────────┘        │        │ │
│  │                              └───────────────────────────────┘        │ │
│  └────────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Project Structure

```
.
├── shop-management/                    # Server A - Orchestration Service (Port 8080)
│   ├── src/main/java/
│   │   └── com/shop/management/
│   │       ├── ShopManagementApplication.java
│   │       ├── config/                 # WebClient, SOAP configurations
│   │       ├── aspect/                 # AOP logging aspects
│   │       ├── controller/             # REST endpoints
│   │       ├── service/                # Business logic & orchestration
│   │       ├── client/                 # REST, SOAP, GraphQL clients
│   │       ├── dto/                    # Data transfer objects
│   │       └── exception/              # Global exception handling
│   └── pom.xml
│
├── product-stock/                      # Server B - Backend Service (Port 8081)
│   ├── src/main/java/
│   │   └── com/productstock/
│   │       ├── ProductStockApplication.java
│   │       ├── config/                 # SOAP, GraphQL configurations
│   │       ├── aspect/                 # AOP logging aspects
│   │       ├── controller/             # REST, GraphQL controllers
│   │       ├── service/                # Business logic
│   │       ├── entity/                 # JPA entities
│   │       ├── repository/             # Data access layer
│   │       └── dto/                    # Data transfer objects
│   ├── src/main/resources/
│   │   └── graphql/schema.graphqls    # GraphQL schema
│   └── pom.xml
│
├── infrastructure/
│   └── terraform/
│       ├── main.tf                     # AWS resources (VPC, EC2, RDS, SG)
│       └── terraform.tfvars.example    # Configuration template
│
├── scripts/
│   ├── deploy_all.sh                   # Full deployment orchestrator
│   ├── deploy_shop.sh                  # Server A deployment
│   ├── deploy_stock.sh                 # Server B deployment
│   ├── stop_shop.sh                    # Stop Server A
│   ├── stop_stock.sh                   # Stop Server B
│   └── .env.example                    # Environment variables template
│
├── postman/
│   ├── Microservices-API-Collection.postman_collection.json
│   ├── Local-Development.postman_environment.json
│   └── AWS-Production.postman_environment.json
│
└── README.md
```

## Communication Protocols

| Protocol | Use Cases | Library |
|----------|-----------|---------|
| **REST** | 1, 2, 3, 8, 9, 10 | Spring WebClient |
| **SOAP** | 4, 5 | Apache CXF 4.0.3 |
| **GraphQL** | 6, 7 | Spring GraphQL |

## 10 Use Cases Implemented

| # | Use Case | Protocol | Method | Endpoint |
|---|----------|----------|--------|----------|
| 1 | Check Stock Availability | REST | GET | `/api/shop/stock/availability` |
| 2 | Reserve Stock for Order | REST | POST | `/api/shop/stock/reserve` |
| 3 | Confirm Stock Deduction | REST | POST | `/api/shop/stock/confirm` |
| 4 | Bulk Stock Update | SOAP | POST | `/api/shop/warehouse/bulk-update` |
| 5 | Warehouse Status | SOAP | GET | `/api/shop/warehouse/{id}/status` |
| 6 | Product Details with Stock | GraphQL | GET | `/api/shop/products/{id}/details` |
| 7 | Report Damaged Returns | GraphQL | POST | `/api/shop/products/damaged-return` |
| 8 | Release Reserved Stock | REST | POST | `/api/shop/stock/release` |
| 9 | Low Stock Alerts | REST | GET | `/api/shop/stock/low-stock-alerts` |
| 10 | Stock Movement History | REST | GET | `/api/shop/stock/movements` |

## AOP Logging

Centralized logging with full fidelity captures:
- HTTP request/response (method, URI, headers, body, status code)
- Outgoing service calls (REST, SOAP, GraphQL)
- Execution time for all operations
- Correlation IDs for distributed tracing

Log locations:
- Server A: `/var/log/shop-app/`
- Server B: `/var/log/stock-app/`

## Quick Start

### Prerequisites
- Java 21 (Amazon Corretto recommended)
- Maven 3.9+
- AWS CLI configured with appropriate permissions
- Terraform 1.5+
- SSH key pair for EC2 access

### Local Development

```bash
# Start product-stock (Server B) first
cd product-stock
mvn clean spring-boot:run

# In another terminal, start shop-management (Server A)
cd shop-management
mvn clean spring-boot:run
```

### Deploy to AWS

1. **Configure Terraform:**
```bash
cd infrastructure/terraform
cp terraform.tfvars.example terraform.tfvars
# Edit terraform.tfvars with your values
```

2. **Apply Infrastructure:**
```bash
terraform init
terraform plan
terraform apply
```

3. **Note Terraform Outputs:**
```
shop_server_public_ip  = "x.x.x.x"
stock_server_public_ip = "y.y.y.y"
stock_server_private_ip = "10.0.2.x"
rds_endpoint = "microservices-db.xxxxx.us-east-1.rds.amazonaws.com"
```

4. **Configure Deployment:**
```bash
cd ../../scripts
cp .env.example .env
# Edit .env with Terraform output values
```

5. **Run Deployment:**
```bash
source .env
./deploy_all.sh
```

## How Server A Knows Server B's Private IP

The critical configuration is the `STOCK_SERVICE_HOST` environment variable:

1. **Terraform** creates both EC2 instances and outputs the private IP of Server B
2. **deploy_all.sh** script receives `STOCK_SERVER_PRIVATE_IP` as an environment variable
3. When deploying Server A, the script passes `STOCK_SERVICE_HOST` pointing to Server B's private IP
4. **shop-management** application uses `STOCK_SERVICE_HOST` in its configuration:

```yaml
# shop-management/src/main/resources/application.yml
stock-service:
  host: ${STOCK_SERVICE_HOST:localhost}
  port: ${STOCK_SERVICE_PORT:8081}
  base-url: http://${stock-service.host}:${stock-service.port}/api/stock
```

## Security Groups

| Security Group | Inbound Rules |
|---------------|---------------|
| shop_server_sg | Port 8080 from 0.0.0.0/0, Port 22 for SSH |
| stock_server_sg | Port 8081 from shop_server_sg only, Port 22 for SSH |
| rds_sg | Port 5432 from shop_server_sg and stock_server_sg |

## API Testing with Postman

1. Import `postman/Microservices-API-Collection.postman_collection.json`
2. Import appropriate environment file:
   - `Local-Development.postman_environment.json` for local testing
   - `AWS-Production.postman_environment.json` for AWS (update IPs)
3. Run collection to test all 10 use cases

## Environment Variables

### Server A (shop-management)
| Variable | Description | Example |
|----------|-------------|---------|
| RDS_ENDPOINT | Database hostname | microservices-db.xxx.rds.amazonaws.com |
| DB_USERNAME | Database user | postgres |
| DB_PASSWORD | Database password | secure-password |
| STOCK_SERVICE_HOST | Server B private IP | 10.0.2.100 |
| STOCK_SERVICE_PORT | Server B port | 8081 |

### Server B (product-stock)
| Variable | Description | Example |
|----------|-------------|---------|
| RDS_ENDPOINT | Database hostname | microservices-db.xxx.rds.amazonaws.com |
| DB_USERNAME | Database user | postgres |
| DB_PASSWORD | Database password | secure-password |

## Technology Stack

- **Java 21** (Amazon Corretto)
- **Spring Boot 3.2.1**
- **Spring WebFlux** (WebClient for non-blocking HTTP)
- **Spring Data JPA** (PostgreSQL persistence)
- **Spring GraphQL** (GraphQL queries/mutations)
- **Apache CXF 4.0.3** (SOAP web services)
- **Logback + Logstash Encoder** (Structured JSON logging)
- **Terraform** (Infrastructure as Code)
- **PostgreSQL** (RDS db.t3.micro)

## Monitoring

Access application health endpoints:
- Shop Management: `http://<shop-ip>:8080/api/shop/health`
- Product Stock: `http://<stock-ip>:8081/api/stock/health`

Spring Actuator endpoints available at `/actuator/*`

## License

MIT License
