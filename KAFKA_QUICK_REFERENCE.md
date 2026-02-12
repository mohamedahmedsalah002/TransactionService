# Kafka Quick Reference - Transaction Service

## Topics Overview

### 📥 CONSUMED (from VirtualBank):
| Topic | Description | When Triggered |
|-------|-------------|----------------|
| `user.created` | New user registered | POST /users |
| `user.updated` | User info changed | PUT /users/{userId} |
| `user.deleted` | User account removed | DELETE /users/{userId} |

### 📤 PRODUCED (by Transaction):
| Topic | Description | When Triggered |
|-------|-------------|----------------|
| `transaction.created` | Transaction initiated | Any transaction endpoint |
| `transaction.completed` | Transaction succeeded | Successful transaction |
| `transaction.failed` | Transaction failed | Failed transaction |

---

## Quick Start Commands

### 1. Start Kafka

```bash
# Terminal 1: Zookeeper
cd /path/to/kafka
bin/zookeeper-server-start.sh config/zookeeper.properties

# Terminal 2: Kafka
bin/kafka-server-start.sh config/server.properties
```

### 2. Start Services

```bash
# Terminal 3: VirtualBank
cd VirtualBank
./mvnw spring-boot:run

# Terminal 4: Transaction
cd Transaction
./mvnw spring-boot:run
```

### 3. Monitor Events (Optional)

```bash
# Terminal 5: Monitor user events
bin/kafka-console-consumer.sh --topic user.created --from-beginning --bootstrap-server localhost:9092

# Terminal 6: Monitor transaction events
bin/kafka-console-consumer.sh --topic transaction.completed --from-beginning --bootstrap-server localhost:9092
```

---

## Test Scenarios

### ✅ Test 1: User Created Event

**Step 1:** Create user in VirtualBank
```bash
curl -X POST http://localhost:8081/users \
  -H "Content-Type: application/json" \
  -d '{"username": "test_user", "email": "test@example.com", "password": "pass123"}'
```

**Expected Result:**
```
Transaction service logs:
✓ Received USER_CREATED event from topic: user.created
✓ Processing USER_CREATED event - UserId: xxx, Username: test_user
✓ Successfully processed USER_CREATED event
```

---

### ✅ Test 2: Transaction Events

**Step 1:** Login
```bash
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "test_user", "password": "pass123"}'
```
**Copy the token from response**

**Step 2:** Make deposit
```bash
curl -X POST http://localhost:8080/api/transactions/deposit \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"accountId": 1, "amount": 1000.00}'
```

**Expected Result:**
```
Transaction service logs:
✓ Publishing TRANSACTION_CREATED event for transactionId: 1
✓ Successfully published TRANSACTION_CREATED event. Offset: X
✓ Publishing TRANSACTION_COMPLETED event for transactionId: 1
✓ Successfully published TRANSACTION_COMPLETED event. Offset: Y
```

---

### ✅ Test 3: User Updated Event

**Step 1:** Update user
```bash
curl -X PUT http://localhost:8081/users/USER_UUID \
  -H "Content-Type: application/json" \
  -d '{"username": "updated_user", "email": "updated@example.com"}'
```

**Expected Result:**
```
Transaction service logs:
✓ Received USER_UPDATED event from topic: user.updated
✓ Processing USER_UPDATED event - UserId: xxx, Username: updated_user
✓ Successfully processed USER_UPDATED event
```

---

### ✅ Test 4: User Deleted Event

**Step 1:** Delete user
```bash
curl -X DELETE http://localhost:8081/users/USER_UUID
```

**Expected Result:**
```
Transaction service logs:
✓ Received USER_DELETED event from topic: user.deleted
✓ Processing USER_DELETED event - UserId: xxx, Reason: User deleted by request
✓ Successfully processed USER_DELETED event
```

---

## Kafka Management Commands

### View All Topics
```bash
bin/kafka-topics.sh --list --bootstrap-server localhost:9092
```

### Create Topic Manually
```bash
bin/kafka-topics.sh --create --topic my-topic \
  --bootstrap-server localhost:9092 \
  --partitions 3 --replication-factor 1
```

### Delete Topic
```bash
bin/kafka-topics.sh --delete --topic my-topic \
  --bootstrap-server localhost:9092
```

### View Topic Details
```bash
bin/kafka-topics.sh --describe --topic transaction.completed \
  --bootstrap-server localhost:9092
```

### View Consumer Group Details
```bash
bin/kafka-consumer-groups.sh --describe \
  --group transaction-service-group \
  --bootstrap-server localhost:9092
```

### Read Messages from Beginning
```bash
bin/kafka-console-consumer.sh \
  --topic transaction.completed \
  --from-beginning \
  --bootstrap-server localhost:9092
```

### Read Latest Messages Only
```bash
bin/kafka-console-consumer.sh \
  --topic transaction.completed \
  --bootstrap-server localhost:9092
```

---

## Event Structures

### User Created Event
```json
{
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

### Transaction Completed Event
```json
{
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

## Configuration Summary

### application.properties (Transaction Service)
```properties
# Kafka Bootstrap
spring.kafka.bootstrap-servers=localhost:9092

# Consumer
spring.kafka.consumer.group-id=transaction-service-group
spring.kafka.consumer.auto-offset-reset=earliest

# Producer
spring.kafka.producer.acks=all
spring.kafka.producer.retries=3
```

---

## Troubleshooting

### ❌ Consumer not receiving messages
```bash
# Check if Kafka is running
jps | grep Kafka

# Check if topic exists
bin/kafka-topics.sh --list --bootstrap-server localhost:9092

# Check consumer group
bin/kafka-consumer-groups.sh --describe \
  --group transaction-service-group \
  --bootstrap-server localhost:9092
```

### ❌ Connection refused error
```
1. Check Kafka is running on localhost:9092
2. Check firewall settings
3. Verify application.properties bootstrap-servers config
```

### ❌ Deserialization errors
```
1. Check event class structure matches between services
2. Verify Jackson configuration
3. Check trusted.packages includes event packages
```

---

## Key Classes

| Class | Purpose | Location |
|-------|---------|----------|
| `Topics.java` | Define topic names | `common/constant/` |
| `UserEventConsumer.java` | Listen to user events | `event/consumer/` |
| `TransactionEventProducer.java` | Publish transaction events | `event/producer/` |
| `KafkaConsumerConfig.java` | Consumer configuration | `config/` |
| `KafkaProducerConfig.java` | Producer configuration | `config/` |

---

## Monitoring Checklist

- [ ] Kafka is running (check with `jps | grep Kafka`)
- [ ] Both services are running (8080 and 8081)
- [ ] Topics are created (check with `--list`)
- [ ] Consumer group is active (check with `--describe`)
- [ ] No errors in service logs
- [ ] Events are being published (monitor with console consumer)
- [ ] Consumer lag is zero or minimal

---

## Performance Tips

1. **Increase partitions** for higher throughput
   ```bash
   --partitions 3
   ```

2. **Batch configuration** for producer
   ```properties
   spring.kafka.producer.batch-size=16384
   spring.kafka.producer.linger.ms=10
   ```

3. **Increase consumer threads**
   ```properties
   spring.kafka.listener.concurrency=3
   ```

4. **Adjust fetch size**
   ```properties
   spring.kafka.consumer.max-poll-records=500
   ```

---

## Summary

✅ **6 Topics** configured (3 consumed, 3 produced)
✅ **Consumer Group**: transaction-service-group
✅ **Bootstrap Server**: localhost:9092
✅ **Auto-offset Reset**: earliest (reads from beginning)
✅ **Serialization**: JSON for both producer and consumer

Your Kafka integration is complete! 🚀
