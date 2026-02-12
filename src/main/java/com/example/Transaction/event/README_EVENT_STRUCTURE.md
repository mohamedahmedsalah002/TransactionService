# Event Package Structure

## Producer (2 files)

- **BaseProducer** (interface)  
  - `sendEvent(String topic, BaseEvent event)`  
  - `sendEvent(String topic, String key, BaseEvent event)`  
  - All producers use this contract.

- **TransactionEventProducer** (implements BaseProducer)  
  - Sends transaction events to Kafka.  
  - `publishTransactionCreated(TransactionCreatedEvent)`  
  - `publishTransactionCompleted(TransactionCompletedEvent)`  
  - `publishTransactionFailed(TransactionFailedEvent)`  
  - Used by `TransactionService` after each transaction.

## Consumer (2 files)

- **BaseConsumer** (interface)  
  - `processEvent(BaseEvent event)`  
  - `handleError(BaseEvent event, Throwable throwable)`  
  - `canHandle(String eventType)`  
  - All consumers use this contract.

- **UserEventConsumer** (implements BaseConsumer)  
  - Listens to **VirtualBank** user events.  
  - Topics: `user.created`, `user.updated`, `user.deleted`.  
  - Group: `transaction-service-group`.

- **TransactionEventConsumer** (implements BaseConsumer)  
  - Listens to **this service’s** transaction events (“listen to yourself”).  
  - Topics: `transaction.created`, `transaction.completed`, `transaction.failed`.  
  - Group: `transaction-internal-group`.  
  - Use for: analytics, audit, notifications, cache invalidation, etc.

## Flow

1. **TransactionService** runs a transaction and calls **TransactionEventProducer**.
2. **TransactionEventProducer** publishes to `transaction.created` / `transaction.completed` / `transaction.failed`.
3. **TransactionEventConsumer** (same app) consumes those events and runs `processEvent(...)`.
4. **UserEventConsumer** consumes user events from VirtualBank and runs `processEvent(...)`.

## Adding a new producer

1. Create a class that `implements BaseProducer`.
2. Inject `KafkaTemplate<String, Object>`.
3. Implement `sendEvent` and add any specific `publish*` methods.

## Adding a new consumer

1. Create a class that `implements BaseConsumer`.
2. Use `@KafkaListener(topics = "...", groupId = "...")` on methods that receive `String` (JSON).
3. Deserialize with `ObjectMapper.readValue(message, YourEventClass.class)` and call `processEvent(event)`.
4. Implement `processEvent`, `handleError`, and `canHandle`.
