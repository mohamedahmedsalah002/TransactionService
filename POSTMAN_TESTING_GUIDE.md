# 🧪 Postman Testing Guide - Transaction API

Complete guide to test all API endpoints using Postman.

---

## 📋 Prerequisites

1. ✅ MySQL running on `localhost:3306`
2. ✅ Kafka running on `localhost:9092` (optional for testing)
3. ✅ Spring Boot application started

### Start the Application
```bash
# From project root directory
./mvnw spring-boot:run

# OR if using an IDE, run TransactionApplication.java
```

**Verify**: Application should start on `http://localhost:8080`

---

## 🔧 Postman Setup

### Base URL
```
http://localhost:8080
```

### Headers (for all requests)
```
Content-Type: application/json
Accept: application/json
```

---

## 📬 API Endpoints Testing

### 1️⃣ Deposit Money (POST)

**Endpoint**: `POST http://localhost:8080/api/transactions/deposit`

**Request Body**:
```json
{
  "accountId": 1,
  "amount": 100.00
}
```

**Expected Response** (201 Created):
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

#### Postman Steps:
1. Click **New** → **HTTP Request**
2. Select **POST** method
3. Enter URL: `http://localhost:8080/api/transactions/deposit`
4. Go to **Headers** tab:
   - Key: `Content-Type`, Value: `application/json`
5. Go to **Body** tab:
   - Select **raw**
   - Select **JSON** from dropdown
   - Paste the JSON request body
6. Click **Send**

---

### 2️⃣ Withdraw Money (POST)

**Endpoint**: `POST http://localhost:8080/api/transactions/withdraw`

**Request Body**:
```json
{
  "accountId": 1,
  "amount": 50.00
}
```

**Expected Response** (201 Created):
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

#### Postman Steps:
1. Create new request or duplicate previous
2. Select **POST** method
3. Enter URL: `http://localhost:8080/api/transactions/withdraw`
4. Set **Headers**: `Content-Type: application/json`
5. Set **Body** to **raw** JSON
6. Paste the JSON request body
7. Click **Send**

---

### 3️⃣ Transfer Money (POST)

**Endpoint**: `POST http://localhost:8080/api/transactions/transfer`

**Request Body**:
```json
{
  "fromAccountId": 1,
  "toAccountId": 2,
  "amount": 75.00
}
```

**Expected Response** (201 Created):
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

#### Postman Steps:
1. Create new request
2. Select **POST** method
3. Enter URL: `http://localhost:8080/api/transactions/transfer`
4. Set **Headers**: `Content-Type: application/json`
5. Set **Body** to **raw** JSON
6. Paste the JSON request body
7. Click **Send**

---

### 4️⃣ Get Transaction by ID (GET)

**Endpoint**: `GET http://localhost:8080/api/transactions/{id}`

**Example**: `GET http://localhost:8080/api/transactions/1`

**No Request Body Needed**

**Expected Response** (200 OK):
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

#### Postman Steps:
1. Create new request
2. Select **GET** method
3. Enter URL: `http://localhost:8080/api/transactions/1`
4. No body needed
5. Click **Send**

---

### 5️⃣ Get Transaction History by Account (GET)

**Endpoint**: `GET http://localhost:8080/api/transactions/account/{accountId}`

**Example**: `GET http://localhost:8080/api/transactions/account/1`

**No Request Body Needed**

**Expected Response** (200 OK):
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

#### Postman Steps:
1. Create new request
2. Select **GET** method
3. Enter URL: `http://localhost:8080/api/transactions/account/1`
4. No body needed
5. Click **Send**

---

## 🧪 Complete Testing Scenario

### Test Flow (Follow this order):

#### Step 1: Deposit $500 to Account 1
```json
POST http://localhost:8080/api/transactions/deposit
{
  "accountId": 1,
  "amount": 500.00
}
```
✅ Expected: Transaction ID returned, status COMPLETED

#### Step 2: Deposit $300 to Account 2
```json
POST http://localhost:8080/api/transactions/deposit
{
  "accountId": 2,
  "amount": 300.00
}
```
✅ Expected: Transaction ID returned, status COMPLETED

#### Step 3: Transfer $150 from Account 1 to Account 2
```json
POST http://localhost:8080/api/transactions/transfer
{
  "fromAccountId": 1,
  "toAccountId": 2,
  "amount": 150.00
}
```
✅ Expected: Transaction ID returned, status COMPLETED

#### Step 4: Withdraw $100 from Account 1
```json
POST http://localhost:8080/api/transactions/withdraw
{
  "accountId": 1,
  "amount": 100.00
}
```
✅ Expected: Transaction ID returned, status COMPLETED

#### Step 5: Get Transaction by ID
```
GET http://localhost:8080/api/transactions/1
```
✅ Expected: Returns the first deposit transaction

#### Step 6: Get All Transactions for Account 1
```
GET http://localhost:8080/api/transactions/account/1
```
✅ Expected: Returns all transactions involving Account 1 (deposit, transfer, withdraw)

---

## ❌ Error Testing

### Test 1: Invalid Amount (Negative)
```json
POST http://localhost:8080/api/transactions/deposit
{
  "accountId": 1,
  "amount": -50.00
}
```
**Expected Response** (400 Bad Request):
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

### Test 2: Missing Required Field
```json
POST http://localhost:8080/api/transactions/deposit
{
  "accountId": 1
}
```
**Expected Response** (400 Bad Request):
```json
{
  "timestamp": "2026-02-09T10:30:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Invalid input parameters",
  "validationErrors": {
    "amount": "Amount is required"
  }
}
```

### Test 3: Transfer to Same Account
```json
POST http://localhost:8080/api/transactions/transfer
{
  "fromAccountId": 1,
  "toAccountId": 1,
  "amount": 50.00
}
```
**Expected Response** (400 Bad Request):
```json
{
  "timestamp": "2026-02-09T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Cannot transfer to the same account"
}
```

### Test 4: Non-existent Transaction
```
GET http://localhost:8080/api/transactions/99999
```
**Expected Response** (500 Internal Server Error):
```json
{
  "timestamp": "2026-02-09T10:30:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Transaction not found with id: 99999"
}
```

---

## 📦 Postman Collection

### Save as Collection

Create a Postman Collection named "Transaction API" with these folders:

```
Transaction API/
├── Deposit/
│   └── POST Deposit Money
├── Withdraw/
│   └── POST Withdraw Money
├── Transfer/
│   └── POST Transfer Money
├── Query/
│   ├── GET Transaction by ID
│   └── GET Transaction History
└── Error Tests/
    ├── Invalid Amount
    ├── Missing Field
    ├── Same Account Transfer
    └── Non-existent Transaction
```

---

## 🔍 Verify Results

### Check Database
```sql
-- Connect to MySQL
mysql -u root -p

-- Use database
USE transaction_db;

-- View all transactions
SELECT * FROM transactions ORDER BY timestamp DESC;

-- View transactions by account
SELECT * FROM transactions 
WHERE from_account = 1 OR to_account = 1 
ORDER BY timestamp DESC;
```

### Check Kafka Events (Optional)
```bash
# Start Kafka console consumer
bin/kafka-console-consumer.sh \
  --topic transaction.completed \
  --from-beginning \
  --bootstrap-server localhost:9092
```

### Check Application Logs
Look for log messages like:
```
Processing deposit for account: 1, amount: 100.00
Deposit completed successfully: 1
Transaction completed event sent to Kafka: 1
```

---

## 📊 Quick Reference Table

| Endpoint | Method | URL | Body Required | Status Code |
|----------|--------|-----|---------------|-------------|
| Deposit | POST | /api/transactions/deposit | ✅ Yes | 201 |
| Withdraw | POST | /api/transactions/withdraw | ✅ Yes | 201 |
| Transfer | POST | /api/transactions/transfer | ✅ Yes | 201 |
| Get by ID | GET | /api/transactions/{id} | ❌ No | 200 |
| Get History | GET | /api/transactions/account/{accountId} | ❌ No | 200 |

---

## 🎯 Common Issues & Solutions

### Issue 1: Connection Refused
**Error**: `Could not connect to localhost:8080`  
**Solution**: Make sure Spring Boot application is running

### Issue 2: 404 Not Found
**Error**: `404 Not Found`  
**Solution**: Check URL spelling, ensure it starts with `/api/transactions`

### Issue 3: 400 Bad Request
**Error**: `400 Bad Request - Validation Failed`  
**Solution**: Check JSON body format, ensure all required fields are present

### Issue 4: 500 Internal Server Error
**Error**: `500 Internal Server Error`  
**Solution**: Check application logs, ensure MySQL is running

### Issue 5: No Response
**Error**: Request hangs/no response  
**Solution**: 
- Check if MySQL is running
- Check if database `transaction_db` exists
- Restart Spring Boot application

---

## 💡 Pro Tips

1. **Use Variables**: In Postman, create environment variables:
   - `base_url` = `http://localhost:8080`
   - `account_id_1` = `1`
   - `account_id_2` = `2`

2. **Save Transaction IDs**: After creating a transaction, save the ID to test GET endpoints

3. **Use Tests Tab**: Add Postman tests to auto-verify responses:
   ```javascript
   pm.test("Status code is 201", function () {
       pm.response.to.have.status(201);
   });
   
   pm.test("Response has transaction ID", function () {
       var jsonData = pm.response.json();
       pm.expect(jsonData.transactionId).to.exist;
   });
   ```

4. **Export Collection**: Save your Postman collection and share with team

5. **Use Pre-request Scripts**: Auto-generate timestamps or random amounts

---

## ✅ Testing Checklist

- [ ] Application is running
- [ ] MySQL is connected
- [ ] Postman is installed
- [ ] Base URL is correct
- [ ] Headers are set
- [ ] Can create deposit
- [ ] Can create withdrawal
- [ ] Can create transfer
- [ ] Can retrieve transaction by ID
- [ ] Can retrieve transaction history
- [ ] Error handling works
- [ ] Validation works
- [ ] Timestamps are correct
- [ ] Transaction IDs are unique

---

**Happy Testing! 🚀**
