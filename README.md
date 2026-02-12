# Transaction Management System

A Spring Boot application for managing financial transactions with Kafka event streaming.

## Features

- **Transaction Entity**: Complete entity with all required fields (transactionId, fromAccount, toAccount, amount, type, timestamp, status)
- **DTOs and Mappers**: Clean separation between entities and DTOs
- **Repository Layer**: JPA repository with custom queries for transaction history
- **Business Logic**: Service layer with validation and transaction management
- **REST API**: Complete RESTful endpoints for all operations
- **Kafka Integration**: Producer for transaction.completed events
- **Exception Handling**: Global exception handler with validation support
- **Logging**: Comprehensive logging using SLF4J

## Technology Stack

- **Spring Boot 4.0.2**
- **Java 21**
- **MySQL 8.x**
- **Apache Kafka**
- **Lombok**
- **JPA/Hibernate**
- **Maven**

## Prerequisites

1. **Java 21** installed
2. **MySQL** server running on localhost:3306
3. **Apache Kafka** server running on localhost:9092
4. **Maven** for building the project

## Database Setup

The application will automatically create the database `transaction_db` if it doesn't exist. Make sure MySQL is running with:

- **Username**: root
- **Password**: root

Or update `application.properties` with your MySQL credentials.

## Kafka Setup

Make sure Kafka is running on `localhost:9092`. The application will publish events to the `transaction.completed` topic.

To start Kafka locally:

```bash
# Start Zookeeper
bin/zookeeper-server-start.sh config/zookeeper.properties

# Start Kafka Server
bin/kafka-server-start.sh config/server.properties

# Create topic (optional, will be auto-created)
bin/kafka-topics.sh --create --topic transaction.completed --bootstrap-server localhost:9092
```

## Running the Application

```bash
# Build the project
./mvnw clean install

# Run the application
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`

## API Endpoints

### 1. Deposit Money (POST)

**Endpoint**: `POST /api/transactions/deposit`

**Status Code**: 201 Created

**Request Body**:
```json
{
  "accountId": 1,
  "amount": 100.00
}
```

**Response**:
```json
{
  "transactionId": 1,
  "fromAccount": 0,
  "toAccount": 1,
  "amount": 100.00,
  "type": "DEPOSIT",
  "timestamp": "2026-02-09T10:30:00",
  "status": "COMPLETED"
}
```

**cURL Example**:
```bash
curl -X POST http://localhost:8080/api/transactions/deposit \
  -H "Content-Type: application/json" \
  -d '{"accountId": 1, "amount": 100.00}'
```

### 2. Withdraw Money (POST)

**Endpoint**: `POST /api/transactions/withdraw`

**Status Code**: 201 Created

**Request Body**:
```json
{
  "accountId": 1,
  "amount": 50.00
}
```

**Response**:
```json
{
  "transactionId": 2,
  "fromAccount": 1,
  "toAccount": 0,
  "amount": 50.00,
  "type": "WITHDRAW",
  "timestamp": "2026-02-09T10:35:00",
  "status": "COMPLETED"
}
```

**cURL Example**:
```bash
curl -X POST http://localhost:8080/api/transactions/withdraw \
  -H "Content-Type: application/json" \
  -d '{"accountId": 1, "amount": 50.00}'
```

### 3. Transfer Money (POST)

**Endpoint**: `POST /api/transactions/transfer`

**Status Code**: 201 Created

**Request Body**:
```json
{
  "fromAccountId": 1,
  "toAccountId": 2,
  "amount": 75.00
}
```

**Response**:
```json
{
  "transactionId": 3,
  "fromAccount": 1,
  "toAccount": 2,
  "amount": 75.00,
  "type": "TRANSFER",
  "timestamp": "2026-02-09T10:40:00",
  "status": "COMPLETED"
}
```

**cURL Example**:
```bash
curl -X POST http://localhost:8080/api/transactions/transfer \
  -H "Content-Type: application/json" \
  -d '{"fromAccountId": 1, "toAccountId": 2, "amount": 75.00}'
```

### 4. Get Transaction by ID (GET)

**Endpoint**: `GET /api/transactions/{id}`

**Status Code**: 200 OK

**Response**:
```json
{
  "transactionId": 1,
  "fromAccount": 0,
  "toAccount": 1,
  "amount": 100.00,
  "type": "DEPOSIT",
  "timestamp": "2026-02-09T10:30:00",
  "status": "COMPLETED"
}
```

**cURL Example**:
```bash
curl -X GET http://localhost:8080/api/transactions/1
```

### 5. Get Transaction History by Account (GET)

**Endpoint**: `GET /api/transactions/account/{accountId}`

**Status Code**: 200 OK

**Response**:
```json
[
  {
    "transactionId": 3,
    "fromAccount": 1,
    "toAccount": 2,
    "amount": 75.00,
    "type": "TRANSFER",
    "timestamp": "2026-02-09T10:40:00",
    "status": "COMPLETED"
  },
  {
    "transactionId": 2,
    "fromAccount": 1,
    "toAccount": 0,
    "amount": 50.00,
    "type": "WITHDRAW",
    "timestamp": "2026-02-09T10:35:00",
    "status": "COMPLETED"
  },
  {
    "transactionId": 1,
    "fromAccount": 0,
    "toAccount": 1,
    "amount": 100.00,
    "type": "DEPOSIT",
    "timestamp": "2026-02-09T10:30:00",
    "status": "COMPLETED"
  }
]
```

**cURL Example**:
```bash
curl -X GET http://localhost:8080/api/transactions/account/1
```

## Transaction Types

- **DEPOSIT**: Money added to an account (fromAccount = 0, system account)
- **WITHDRAW**: Money removed from an account (toAccount = 0, system account)
- **TRANSFER**: Money moved between accounts

## Transaction Status

- **PENDING**: Transaction initiated
- **COMPLETED**: Transaction successful
- **FAILED**: Transaction failed
- **CANCELLED**: Transaction cancelled

## Kafka Events

When a transaction is completed, an event is published to the `transaction.completed` topic:

**Event Format**:
```json
{
  "transactionId": 1,
  "type": "DEPOSIT",
  "amount": 100.00,
  "status": "COMPLETED",
  "timestamp": "2026-02-09T10:30:00"
}
```

**Consume Events**:
```bash
bin/kafka-console-consumer.sh --topic transaction.completed \
  --from-beginning --bootstrap-server localhost:9092
```

## Validation

All endpoints validate input:

- Amount must be greater than 0
- Account IDs are required
- Cannot transfer to the same account
- Proper error messages returned for validation failures

## Error Handling

The application includes global exception handling:

**Validation Error (400)**:
```json
{
  "timestamp": "2026-02-09T10:30:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Invalid input parameters",
  "validationErrors": {
    "amount": "Amount must be greater than 0"
  }
}
```

**Runtime Error (500)**:
```json
{
  "timestamp": "2026-02-09T10:30:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Transaction not found with id: 999"
}
```

## Project Structure

```
src/main/java/com/example/Transaction/
├── config/
│   └── KafkaProducerConfig.java          # Kafka producer configuration
├── controller/
│   └── TransactionController.java         # REST endpoints
├── exception/
│   ├── ErrorResponse.java                 # Error response DTO
│   └── GlobalExceptionHandler.java        # Exception handling
├── mapper/
│   └── TransactionMapper.java             # Entity-DTO mapping
├── model/
│   ├── dto/
│   │   └── TransactionDto.java            # DTOs
│   └── entity/
│       └── Transaction.java               # JPA entity
├── repository/
│   └── TransactionReppsitory.java         # JPA repository
├── service/
│   └── TransactionService.java            # Business logic
└── TransactionApplication.java            # Main application
```

## Testing with Postman

Import the following collection to test all endpoints:

1. Create a new collection in Postman
2. Add requests for each endpoint above
3. Set `Content-Type: application/json` header
4. Test the complete workflow: Deposit → Transfer → Withdraw → Get History

## Future Enhancements

- Add authentication and authorization
- Implement account balance validation
- Add transaction rollback mechanism
- Implement idempotency for transactions
- Add pagination for transaction history
- Add filtering and sorting options
- Implement transaction limits and throttling
- Add comprehensive unit and integration tests
- Implement distributed tracing
- Add metrics and monitoring

## Notes

- The repository has a typo in the name (`TransactionReppsitory` instead of `TransactionRepository`), but it's maintained for compatibility
- System account (ID: 0) is used for deposits and withdrawals
- All transactions are logged for audit purposes
- Kafka failures don't affect transaction completion
- Timestamps are automatically set on transaction creation

## License

This project is a demo application for educational purposes.
