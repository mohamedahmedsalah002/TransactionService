# Kafka Integration Guide - Transaction Service

## Overview

The Transaction service now has full Kafka integration with:
- **Consumer**: Listens to user events from VirtualBank
- **Producer**: Publishes transaction events

## Architecture

```
VirtualBank Service                    Transaction Service
     (Port 8081)                           (Port 8080)
         |                                      |
         |---> user.created ------------------>|
         |---> user.updated ------------------>| CONSUMER
         |---> user.deleted ------------------>|
         |                                      |
         |<--- transaction.created ------------|
         |<--- transaction.completed ----------| PRODUCER
         |<--- transaction.failed -------------|
                        ^
                        |
                    KAFKA BROKER
                   (localhost:9092)
```

---

## Kafka Topics

### Consumed Topics (from VirtualBank):
1. **user.created** - New user registration
2. **user.updated** - User information updated
3. **user.deleted** - User account deleted

### Produced Topics (by Transaction):
1. **transaction.created** - Transaction initiated
2. **transaction.completed** - Transaction successfully completed
3. **transaction.failed** - Transaction failed

---

## Event Flow Examples

### Example 1: User Registration → Transaction

1. **User registers in VirtualBank**
   ```
   POST http://localhost:8081/users
   Body: {"username": "alice", "email": "alice@test.com", "password": "pass123"}
   ```

2. **VirtualBank publishes event**
   ```
   Topic: user.created
   Event: {
     "eventId": "uuid",
     "eventType": "USER_CREATED",
     "timestamp": "2026-02-11T20:00:00",
     "serviceName": "user-service",
     "data": {
       "userId": "uuid",
       "username": "alice",
       "email": "alice@test.com"
     }
   }
   ```

3. **Transaction service receives event**
   ```
   LOG: Received USER_CREATED event - UserId: uuid, Username: alice
   LOG: Processing USER_CREATED event...
   LOG: Successfully processed USER_CREATED event
   ```

### Example 2: Transaction → Event Publishing

1. **User makes deposit**
   ```
   POST http://localhost:8080/api/transactions/deposit
   Headers: Authorization: Bearer <token>
   Body: {"accountId": 1, "amount": 1000.00}
   ```

2. **Transaction service publishes events**
   ```
   Topic: transaction.created
   Event: {
     "eventId": "uuid",
     "eventType": "TRANSACTION_CREATED",
     "timestamp": "2026-02-11T20:01:00",
     "serviceName": "transaction-service",
     "data": {
       "transactionId": 1,
       "fromAccount": 1,
       "toAccount": 1,
       "amount": 1000.00,
       "transactionType": "DEPOSIT",
       "status": "COMPLETED"
     }
   }
   ```

   ```
   Topic: transaction.completed
   Event: {
     "eventId": "uuid",
     "eventType": "TRANSACTION_COMPLETED",
     "timestamp": "2026-02-11T20:01:00",
     "serviceName": "transaction-service",
     "data": {
       "transactionId": 1,
       "fromAccount": 1,
       "toAccount": 1,
       "amount": 1000.00,
       "transactionType": "DEPOSIT",
       "completedAt": "2026-02-11T20:01:00"
     }
   }
   ```

---

## Files Created

### Event Models:
```
Transaction/src/main/java/com/example/Transaction/
├── common/constant/
│   └── Topics.java                              ✅ NEW
├── event/
│   ├── model/
│   │   ├── BaseEvent.java                      ✅ NEW
│   │   ├── CreateUserEvent.java                ✅ NEW
│   │   ├── UserUpdatedEvent.java               ✅ NEW
│   │   ├── UserDeletedEvent.java               ✅ NEW
│   │   ├── TransactionCreatedEvent.java        ✅ NEW
│   │   ├── TransactionCompletedEvent.java      ✅ NEW
│   │   └── TransactionFailedEvent.java         ✅ NEW
│   ├── producer/
│   │   └── TransactionEventProducer.java       ✅ NEW
│   └── consumer/
│       └── UserEventConsumer.java              ✅ NEW
└── config/
    ├── KafkaProducerConfig.java                ✅ UPDATED
    ├── KafkaConsumerConfig.java                ✅ NEW
    └── JacksonConfig.java                      ✅ NEW
```

### Modified Files:
- ✅ `TransactionService.java` - Added event publishing
- ✅ `application.properties` - Added Kafka consumer config

---

## Setup Instructions

### 1. Start Kafka

```bash
# Start Zookeeper
cd kafka_installation_directory
bin/zookeeper-server-start.sh config/zookeeper.properties

# Start Kafka (in new terminal)
bin/kafka-server-start.sh config/server.properties
```

### 2. Create Kafka Topics (Optional - auto-created on first use)

```bash
# User topics
bin/kafka-topics.sh --create --topic user.created --bootstrap-server localhost:9092
bin/kafka-topics.sh --create --topic user.updated --bootstrap-server localhost:9092
bin/kafka-topics.sh --create --topic user.deleted --bootstrap-server localhost:9092

# Transaction topics
bin/kafka-topics.sh --create --topic transaction.created --bootstrap-server localhost:9092
bin/kafka-topics.sh --create --topic transaction.completed --bootstrap-server localhost:9092
bin/kafka-topics.sh --create --topic transaction.failed --bootstrap-server localhost:9092
```

### 3. Start Services

```bash
# Terminal 1: Start VirtualBank
cd VirtualBank
./mvnw spring-boot:run

# Terminal 2: Start Transaction Service
cd Transaction
./mvnw spring-boot:run
```

### 4. Monitor Kafka Topics (Optional)

```bash
# Monitor user.created topic
bin/kafka-console-consumer.sh --topic user.created --from-beginning --bootstrap-server localhost:9092

# Monitor transaction.completed topic
bin/kafka-console-consumer.sh --topic transaction.completed --from-beginning --bootstrap-server localhost:9092
```

---

## Testing the Integration

### Test 1: Create User and Verify Event Consumption

1. **Create a user:**
   ```bash
   curl -X POST http://localhost:8081/users \
     -H "Content-Type: application/json" \
     -d '{
       "username": "bob",
       "email": "bob@test.com",
       "password": "pass123"
     }'
   ```

2. **Check Transaction service logs:**
   ```
   Expected output:
   - Received USER_CREATED event from topic: user.created
   - Processing USER_CREATED event - UserId: <uuid>, Username: bob
   - Successfully processed USER_CREATED event
   ```

### Test 2: Make Transaction and Verify Event Publishing

1. **Login to get token:**
   ```bash
   curl -X POST http://localhost:8081/auth/login \
     -H "Content-Type: application/json" \
     -d '{
       "username": "bob",
       "password": "pass123"
     }'
   ```

2. **Make a deposit:**
   ```bash
   curl -X POST http://localhost:8080/api/transactions/deposit \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer <YOUR_TOKEN>" \
     -d '{
       "accountId": 1,
       "amount": 500.00
     }'
   ```

3. **Check Transaction service logs:**
   ```
   Expected output:
   - Publishing TRANSACTION_CREATED event for transactionId: 1
   - Successfully published TRANSACTION_CREATED event
   - Publishing TRANSACTION_COMPLETED event for transactionId: 1
   - Successfully published TRANSACTION_COMPLETED event
   ```

### Test 3: Update User and Verify Event Consumption

1. **Update user:**
   ```bash
   curl -X PUT http://localhost:8081/users/<USER_UUID> \
     -H "Content-Type: application/json" \
     -d '{
       "username": "bob_updated",
       "email": "bob_new@test.com"
     }'
   ```

2. **Check Transaction service logs:**
   ```
   Expected output:
   - Received USER_UPDATED event from topic: user.updated
   - Processing USER_UPDATED event - UserId: <uuid>, Username: bob_updated
   - Successfully processed USER_UPDATED event
   ```

### Test 4: Delete User and Verify Event Consumption

1. **Delete user:**
   ```bash
   curl -X DELETE http://localhost:8081/users/<USER_UUID>
   ```

2. **Check Transaction service logs:**
   ```
   Expected output:
   - Received USER_DELETED event from topic: user.deleted
   - Processing USER_DELETED event - UserId: <uuid>, Reason: User deleted by request
   - Successfully processed USER_DELETED event
   ```

---

## Consumer Group

The Transaction service uses consumer group: `transaction-service-group`

This means:
- Multiple instances of Transaction service can run in parallel
- Kafka will distribute messages across instances
- Each message is processed by only ONE instance

To run multiple instances:
```bash
# Terminal 1
java -jar transaction-service.jar --server.port=8080

# Terminal 2
java -jar transaction-service.jar --server.port=8081

# Terminal 3
java -jar transaction-service.jar --server.port=8082
```

---

## Configuration

### application.properties

```properties
# Kafka Configuration
spring.kafka.bootstrap-servers=localhost:9092

# Producer Configuration
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
spring.kafka.producer.acks=all
spring.kafka.producer.retries=3
spring.kafka.producer.properties.spring.json.add.type.headers=false

# Consumer Configuration
spring.kafka.consumer.group-id=transaction-service-group
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer
spring.kafka.consumer.properties.spring.json.trusted.packages=*
spring.kafka.consumer.properties.spring.json.use.type.headers=false
```

### Key Configuration Explained:

- **auto-offset-reset=earliest**: Reads all messages from beginning if no previous offset
- **group-id**: Identifies this consumer group
- **trusted.packages=***: Allows deserializing all Java packages (use with caution in production)
- **acks=all**: Waits for all replicas to acknowledge before considering message sent
- **retries=3**: Retries sending messages 3 times on failure

---

## Business Logic Implementation

The consumer methods have TODO comments for implementing business logic:

### In `UserEventConsumer.java`:

```java
private void processUserCreated(CreateUserEvent event) {
    // TODO: Implement your business logic here
    // Examples:
    // - Cache user information for faster lookups
    // - Create a local user record if needed
    // - Initialize user-related resources
}

private void processUserUpdated(UserUpdatedEvent event) {
    // TODO: Implement your business logic here
    // Examples:
    // - Update cached user information
    // - Sync user data with local records
    // - Invalidate related caches
}

private void processUserDeleted(UserDeletedEvent event) {
    // TODO: Implement your business logic here
    // Examples:
    // - Remove user from cache
    // - Archive or soft-delete user transactions
    // - Clean up user-related resources
    // - Prevent new transactions for this user
}
```

---

## Error Handling

### Consumer Errors:
- Logged but don't crash the application
- Consider implementing:
  - Dead Letter Queue (DLQ) for failed messages
  - Retry with exponential backoff
  - Alert on repeated failures

### Producer Errors:
- Logged but don't fail the transaction
- Transaction is saved even if event publishing fails
- Consider implementing:
  - Outbox pattern for guaranteed event delivery
  - Retry mechanism
  - Monitoring and alerts

---

## Monitoring and Debugging

### View Consumer Lag:

```bash
bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
  --describe --group transaction-service-group
```

### List All Topics:

```bash
bin/kafka-topics.sh --list --bootstrap-server localhost:9092
```

### View Topic Details:

```bash
bin/kafka-topics.sh --describe --topic transaction.completed \
  --bootstrap-server localhost:9092
```

### Read Messages from Topic:

```bash
# From beginning
bin/kafka-console-consumer.sh --topic transaction.completed \
  --from-beginning --bootstrap-server localhost:9092

# Latest messages only
bin/kafka-console-consumer.sh --topic transaction.completed \
  --bootstrap-server localhost:9092
```

---

## Troubleshooting

### Issue: Consumer not receiving messages

**Check:**
1. Kafka is running: `jps | grep Kafka`
2. Topic exists: `bin/kafka-topics.sh --list --bootstrap-server localhost:9092`
3. Messages are being published: Monitor topic with console consumer
4. Consumer group is active: Check consumer groups
5. Application logs for errors

### Issue: Producer not sending messages

**Check:**
1. Kafka connection in logs
2. JsonSerializer is configured correctly
3. Event objects are serializable
4. No exceptions in logs

### Issue: Deserialization errors

**Check:**
1. Event class structure matches between producer and consumer
2. Jackson configuration is correct
3. Trusted packages includes your event packages
4. JavaTimeModule is registered for LocalDateTime

---

## Best Practices

1. **Idempotency**: Process events idempotently (same event multiple times = same result)
2. **Error Handling**: Never crash on bad messages, log and skip or send to DLQ
3. **Monitoring**: Monitor consumer lag, it indicates if consumer is keeping up
4. **Scaling**: Add more consumer instances if lag increases
5. **Versioning**: Version your events (add version field to handle schema changes)
6. **Testing**: Test with Kafka test containers or embedded Kafka

---

## Next Steps

### Recommended Enhancements:

1. **Implement actual business logic in consumer methods**
2. **Add dead letter queue for failed messages**
3. **Implement event versioning**
4. **Add metrics and monitoring (Micrometer + Prometheus)**
5. **Implement circuit breaker for Kafka failures**
6. **Add integration tests with embedded Kafka**
7. **Implement outbox pattern for guaranteed event delivery**
8. **Add event replay functionality**

---

## Summary

✅ Transaction service now **consumes** user events from VirtualBank
✅ Transaction service **produces** transaction events
✅ Full event-driven architecture implemented
✅ Both services communicate through Kafka
✅ Scalable and loosely coupled microservices

Your Transaction service is now fully integrated with Kafka! 🎉
