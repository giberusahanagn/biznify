Biznify – Warehouse Management System
A microservices-based Warehouse Management System built with Java, Spring Boot, Kafka, MySQL, Docker, and Kubernetes. The project is designed to manage warehouse operations through independently deployable services and asynchronous communication.
Overview
Biznify is a backend microservices project that organizes warehouse-related operations into separate services. Each service owns a focused responsibility and exposes REST APIs. Kafka supports asynchronous communication between services, while Docker provides a repeatable local environment.
Architecture
The system is organized into microservices, including:
- Partner Service – manages partner-related operations.
- Product Service – manages product information.
- Warehouse Service – manages warehouse operations.
- Product Batch Service – handles product batch/inventory-related operations.
- Inbound Shipment Service – supports inbound shipment workflows.
- Outbound Shipment Service – supports outbound shipment workflows.
- Shared Events Module – provides shared event definitions/contracts used across services.
Service scope and API details may evolve as development continues.

Technology Stack
Technology	Purpose
Java	Backend programming language
Spring Boot	Microservice development
Spring MVC / REST	API development
Maven	Build and multi-module project management
MySQL	Relational database
Apache Kafka	Asynchronous event-driven communication
Docker & Docker Compose	Containerization and local environment
Kubernetes	Deployment configuration and orchestration practice
Git	Version control


Project Structure
biznify/
├── partner-service/
├── product-service/
├── warehouse-service/
├── product-batch-service/
├── inbound-shipment-service/
├── outbound-shipment-service/
├── shared-events/
├── docker-compose.yml
└── README.md
The actual directory names and configuration files may differ as the project evolves.
Getting Started
Prerequisites
Install the following:
- JDK (compatible with the project's configured Java version)
- Maven
- Docker Desktop
- Git
Clone the Repository
git clone https://github.com/giberusahanagn/biznify.git
cd biznify
Build the Project
From the root of the Maven multi-module project:
mvn clean install
Start Infrastructure
If Docker Compose is configured for the project, start the required infrastructure:
docker compose up -d
To stop the containers:
docker compose down
Run Individual Services
Run a service from its module directory, or use the root Maven configuration if it supports running modules.

Example:
cd product-service
mvn spring-boot:run
Repeat for the other services as needed. Confirm service ports and database/Kafka configuration in each service's application configuration.
Kafka Integration
Kafka is used to support asynchronous communication between microservices. Shared event definitions are maintained in the shared-events module to reduce duplication and help keep event contracts consistent.
Docker
Services and infrastructure can be containerized to provide a consistent development environment. Docker Compose can be used to start supporting components such as MySQL and Kafka, depending on the current configuration.
Kubernetes
Kubernetes deployment manifests are included as part of the project work and are used to practice deploying and managing containerized infrastructure and services.
Development Highlights
- Designed a Maven multi-module microservices architecture.
- Developed REST APIs using Spring Boot.
- Organized services around warehouse business responsibilities.
- Implemented Kafka-based asynchronous messaging.
- Centralized shared event contracts in a common module.
- Containerized infrastructure and services with Docker.
- Practiced Kubernetes deployment configuration.
- Performed cross-service debugging and testing.
Future Enhancements
- Centralized API Gateway.
- Service discovery and centralized configuration.
- Authentication and authorization integration.
- Automated integration testing.
- Monitoring, metrics, and centralized logging.
- CI/CD pipeline.
Author
Sahana G N

Java Backend Developer | Spring Boot | Microservices | Kafka | Docker

GitHub: giberusahanagn
