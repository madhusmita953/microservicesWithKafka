# Microservices with Kafka - Complete E-Commerce System

A production-ready microservices architecture demonstrating:
- **Spring Boot 3.x** with Spring Cloud
- **Kafka** for asynchronous event streaming
- **Saga Pattern (Choreography)** for distributed transactions
- **Eureka** for service discovery
- **API Gateway** for request routing
- **Spring Cloud Config** for centralized configuration
- **ELK Stack** for centralized logging
- **PostgreSQL** for persistence
- **Docker Compose** for local development

## Project Structure

```
microservicesWithKafka/
├── common/                          # Shared DTOs, events, and utilities
├── config-server/                   # Spring Cloud Config Server
├── service-registry/                # Eureka Service Registry
├── api-gateway/                     # Spring Cloud Gateway
├── order-service/                   # Order microservice
├── payment-service/                 # Payment microservice
├── inventory-service/               # Inventory microservice
├── notification-service/            # Notification microservice
├── docker-compose.yml               # Full stack orchestration
├── config/                          # Centralized configurations
└── docker/                          # Docker configurations for ELK
```

## Event Flow (Choreography-based Saga Pattern)

```
Order Service:
  1. Create Order (PENDING)
  2. Publish: OrderCreatedEvent
     ↓
Payment Service (listens to OrderCreatedEvent):
  3. Process Payment
  4. Publish: PaymentProcessedEvent OR PaymentFailedEvent
     ↓
Inventory Service (listens to PaymentProcessedEvent):
  5. Reserve Inventory
  6. Publish: InventoryReservedEvent OR InventoryReservationFailedEvent
     ↓
Notification Service (listens to all events):
  7. Send notifications based on event type
     ↓
Order Service (listens to final events):
  8. Update Order Status (CONFIRMED/FAILED)
  9. Trigger rollback if needed
```

## Key Features

### 1. Kafka Configuration
- **Topics**: order-created, payment-processed, inventory-reserved, etc.
- **Consumer Groups**: One per service for independent processing
- **Retry Topics**: Automatic retry mechanism
- **Dead Letter Queue (DLQ)**: Failed messages handling

### 2. Saga Pattern Implementation
- Choreography-based (event-driven)
- Automatic compensation/rollback on failure
- Idempotency handling with event store
- Distributed transaction management

### 3. Service Discovery (Eureka)
- All services register with Eureka
- API Gateway uses Eureka for routing
- Health checks and automatic deregistration

### 4. API Gateway
- Central entry point for all requests
- Load balancing across service instances
- Request routing based on URL patterns
- Rate limiting (optional)

### 5. Spring Cloud Config
- Centralized configuration management
- Environment-specific properties
- Real-time configuration updates

### 6. ELK Stack Integration
- **Elasticsearch**: Centralized log storage
- **Logstash**: Log processing and forwarding
- **Kibana**: Log visualization and analysis
- Structured logging with JSON format

### 7. Data Persistence
- **PostgreSQL** for all services
- JPA/Hibernate for ORM
- Database migrations with Flyway

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- Git

### Quick Start

1. **Clone the repository**
   ```bash
   git clone https://github.com/madhusmita953/microservicesWithKafka.git
   cd microservicesWithKafka
   ```

2. **Build the project**
   ```bash
   mvn clean package -DskipTests
   ```

3. **Start all services**
   ```bash
   docker-compose up -d
   ```

4. **Verify services are running**
   ```bash
   # Eureka Dashboard
   http://localhost:8761
   
   # API Gateway
   http://localhost:8080
   
   # Kibana (ELK Stack)
   http://localhost:5601
   ```

### Service Ports
- Config Server: `8888`
- Service Registry (Eureka): `8761`
- API Gateway: `8080`
- Order Service: `8081`
- Payment Service: `8082`
- Inventory Service: `8083`
- Notification Service: `8084`
- Kafka Broker: `9092`
- PostgreSQL: `5432`
- Elasticsearch: `9200`
- Kibana: `5601`

## API Endpoints

### Order Service
```bash
# Create Order
POST http://localhost:8080/api/orders
Content-Type: application/json

{
  "customerId": "CUST001",
  "items": [
    {
      "productId": "PROD001",
      "quantity": 2,
      "price": 50.00
    }
  ],
  "totalAmount": 100.00
}

# Get Order
GET http://localhost:8080/api/orders/{orderId}

# Get All Orders
GET http://localhost:8080/api/orders
```

### Payment Service
```bash
# Get Payment
GET http://localhost:8080/api/payments/{paymentId}
```

### Inventory Service
```bash
# Check Product Stock
GET http://localhost:8080/api/inventory/{productId}

# Get All Inventory
GET http://localhost:8080/api/inventory
```

### Notification Service
```bash
# Get Notifications
GET http://localhost:8080/api/notifications
```

## Monitoring and Logging

### Kibana Dashboard
1. Open http://localhost:5601
2. Go to "Index Management"
3. Look for indices like `logs-*`
4. Create index pattern for visualization
5. Use Dev Tools to query logs

### Eureka Dashboard
- http://localhost:8761
- View all registered services
- Monitor service health

### Kafka Monitoring
```bash
# View Kafka topics
docker exec kafka kafka-topics.sh --list --bootstrap-server localhost:9092

# View messages in a topic
docker exec kafka kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic order-created --from-beginning

# View consumer group lag
docker exec kafka kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group order-service --describe
```

## Architecture Diagrams

### Service Communication Flow
```
┌─────────┐
│ Client  │
└────┬────┘
     │ HTTP/REST
     ▼
┌──────────────┐
│ API Gateway  │ (Port 8080)
└──┬───────┬───┬────┬──────┘
   │       │   │    │
   ▼       ▼   ▼    ▼
┌─────┐ ┌─────┐ ┌────┐ ┌──────┐
│Order│ │Paym │ │Invent│Notif │
│Svc  │ │ent  │ │ory  │ Svc  │
└──┬──┘ └──┬──┘ └──┬──┘ └──┬───┘
   │       │      │       │
   └───────┴──────┴───────┘
        Kafka Topics
        (Event-driven)
```

## Performance Optimization

### Idempotency
- Each event has a unique `correlationId`
- Event store prevents duplicate processing
- Idempotency key in requests

### Retry Mechanism
- Automatic retry on transient failures
- Exponential backoff strategy
- Dead Letter Queue for permanent failures

### Load Balancing
- API Gateway distributes requests
- Eureka health checks
- Kafka consumer groups for parallel processing

## Security Considerations

1. **Service-to-Service Communication**: Use Spring Cloud Security
2. **API Gateway**: Implement JWT/OAuth2 authentication
3. **Database**: Encrypted connections with SSL
4. **Kafka**: Enable SASL/SSL authentication
5. **Logs**: Sanitize sensitive data before logging

## Troubleshooting

### Services not registering with Eureka
```bash
# Check if Eureka is running
curl http://localhost:8761/eureka/apps

# Check service logs
docker logs order-service
```

### Kafka connectivity issues
```bash
# Verify Kafka is running
docker ps | grep kafka

# Check Kafka logs
docker logs kafka
```

### Database connection errors
```bash
# Verify PostgreSQL is running
docker ps | grep postgres

# Check PostgreSQL logs
docker logs postgres
```

## Database Migrations

Each service uses Flyway for database versioning:
```
src/main/resources/db/migration/
├── V1__Create_Tables.sql
└── V2__Add_Indexes.sql
```

## Contributing

1. Create a feature branch
2. Make changes
3. Test thoroughly
4. Create a pull request

## License

MIT License - feel free to use for learning and production

## Support

For issues or questions, please create an issue in the repository.
