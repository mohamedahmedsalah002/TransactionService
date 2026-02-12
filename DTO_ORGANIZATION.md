# 📁 DTO Organization Structure

## ✅ New Organized Structure

The DTOs have been reorganized into **Request** and **Response** folders for better clarity and maintainability.

---

## 📂 Complete DTO Structure

```
src/main/java/com/example/Transaction/model/dto/
├── request/                              ← Request DTOs (Client → Server)
│   ├── DepositRequest.java              ✅ Input for deposit operations
│   ├── WithdrawRequest.java             ✅ Input for withdrawal operations
│   └── TransferRequest.java             ✅ Input for transfer operations
└── response/                             ← Response DTOs (Server → Client)
    └── TransactionDto.java               ✅ Output for all transaction operations
```

---

## 🔄 Request DTOs (Input)

### Package: `com.example.Transaction.model.dto.request`

#### 1. DepositRequest.java
```java
package com.example.Transaction.model.dto.request;

public class DepositRequest {
    @NotNull private Long accountId;
    @NotNull @DecimalMin("0.01") private BigDecimal amount;
}
```
**Purpose**: Accept deposit requests from clients  
**Endpoint**: `POST /api/transactions/deposit`

#### 2. WithdrawRequest.java
```java
package com.example.Transaction.model.dto.request;

public class WithdrawRequest {
    @NotNull private Long accountId;
    @NotNull @DecimalMin("0.01") private BigDecimal amount;
}
```
**Purpose**: Accept withdrawal requests from clients  
**Endpoint**: `POST /api/transactions/withdraw`

#### 3. TransferRequest.java
```java
package com.example.Transaction.model.dto.request;

public class TransferRequest {
    @NotNull private Long fromAccountId;
    @NotNull private Long toAccountId;
    @NotNull @DecimalMin("0.01") private BigDecimal amount;
}
```
**Purpose**: Accept transfer requests from clients  
**Endpoint**: `POST /api/transactions/transfer`

---

## 📤 Response DTOs (Output)

### Package: `com.example.Transaction.model.dto.response`

#### TransactionDto.java
```java
package com.example.Transaction.model.dto.response;

public class TransactionDto {
    private Long transactionId;
    private Long fromAccount;
    private Long toAccount;
    private BigDecimal amount;
    private TransactionType type;
    private LocalDateTime timestamp;
    private TransactionStatus status;
}
```
**Purpose**: Return transaction details to clients  
**Used by**: All transaction endpoints (deposit, withdraw, transfer, get by id, get history)

---

## 🔗 Updated Imports

All files have been updated with the new package structure:

### Controller
```java
import com.example.Transaction.model.dto.request.DepositRequest;
import com.example.Transaction.model.dto.request.WithdrawRequest;
import com.example.Transaction.model.dto.request.TransferRequest;
import com.example.Transaction.model.dto.response.TransactionDto;
```

### Service
```java
import com.example.Transaction.model.dto.request.DepositRequest;
import com.example.Transaction.model.dto.request.WithdrawRequest;
import com.example.Transaction.model.dto.request.TransferRequest;
import com.example.Transaction.model.dto.response.TransactionDto;
```

### Mappers
```java
// DepositMapper
import com.example.Transaction.model.dto.request.DepositRequest;

// WithdrawMapper
import com.example.Transaction.model.dto.request.WithdrawRequest;

// TransferMapper
import com.example.Transaction.model.dto.request.TransferRequest;

// TransactionMapper
import com.example.Transaction.model.dto.response.TransactionDto;
```

---

## 💡 Benefits of This Organization

### 1. **Clear Separation of Concerns**
- `request/` - What clients send to the API
- `response/` - What the API returns to clients

### 2. **Better Scalability**
As your project grows, you can easily add more DTOs:
```
dto/
├── request/
│   ├── DepositRequest.java
│   ├── WithdrawRequest.java
│   ├── TransferRequest.java
│   ├── UpdateAccountRequest.java     ← New
│   └── CloseAccountRequest.java      ← New
└── response/
    ├── TransactionDto.java
    ├── AccountDto.java               ← New
    ├── TransactionSummaryDto.java    ← New
    └── ErrorResponseDto.java         ← New
```

### 3. **Improved Code Navigation**
Developers can instantly find:
- Need a request DTO? → Look in `request/`
- Need a response DTO? → Look in `response/`

### 4. **Better API Documentation**
API documentation tools (like Swagger) will group:
- Request models under one section
- Response models under another section

### 5. **Industry Best Practice**
This structure is used by many enterprise applications:
```
dto/
├── request/    (also known as: command/, input/)
└── response/   (also known as: output/, view/)
```

---

## 📋 Usage Examples

### Deposit Example
```java
// Request
POST /api/transactions/deposit
Body: {
    "accountId": 1,
    "amount": 100.00
}
// Type: DepositRequest (request package)

// Response
Status: 201 Created
Body: {
    "transactionId": 123,
    "fromAccount": 0,
    "toAccount": 1,
    "amount": 100.00,
    "type": "DEPOSIT",
    "timestamp": "2026-02-09T10:30:00",
    "status": "COMPLETED"
}
// Type: TransactionDto (response package)
```

### Transfer Example
```java
// Request
POST /api/transactions/transfer
Body: {
    "fromAccountId": 1,
    "toAccountId": 2,
    "amount": 75.00
}
// Type: TransferRequest (request package)

// Response
Status: 201 Created
Body: {
    "transactionId": 124,
    "fromAccount": 1,
    "toAccount": 2,
    "amount": 75.00,
    "type": "TRANSFER",
    "timestamp": "2026-02-09T10:35:00",
    "status": "COMPLETED"
}
// Type: TransactionDto (response package)
```

---

## ✅ Verification

- ✅ All DTOs moved to appropriate folders
- ✅ All imports updated across the project
- ✅ Controller imports updated
- ✅ Service imports updated
- ✅ All mapper imports updated
- ✅ No compilation errors
- ✅ Package declarations match folder structure

---

## 🎯 Summary

| Folder | DTOs | Purpose | Count |
|--------|------|---------|-------|
| `request/` | DepositRequest, WithdrawRequest, TransferRequest | Client input | 3 |
| `response/` | TransactionDto | Server output | 1 |

**Total DTOs**: 4  
**Status**: ✅ Production Ready

This organization makes your codebase more professional, maintainable, and scalable!
