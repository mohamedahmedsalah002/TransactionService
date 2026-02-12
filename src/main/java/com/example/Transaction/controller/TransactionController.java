package com.example.Transaction.controller;

import com.example.Transaction.model.dto.request.DepositRequest;
import com.example.Transaction.model.dto.request.TransferRequest;
import com.example.Transaction.model.dto.request.WithdrawRequest;
import com.example.Transaction.model.dto.response.BalanceResponse;
import com.example.Transaction.model.dto.response.TransactionDto;
import com.example.Transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Validated
@Slf4j
public class TransactionController {
    
    private final TransactionService transactionService;
    
    @PostMapping("/deposit")
    public ResponseEntity<TransactionDto> deposit(@Valid @RequestBody DepositRequest request) {
        log.info("Received deposit request for account: {}", request.getAccountId());
        
        TransactionDto transaction = transactionService.deposit(
                request.getAccountId(),
                request.getAmount()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }
    
    @PostMapping("/withdraw")
    public ResponseEntity<TransactionDto> withdraw(@Valid @RequestBody WithdrawRequest request) {
        log.info("Received withdrawal request for account: {}", request.getAccountId());
        
        TransactionDto transaction = transactionService.withdraw(
                request.getAccountId(),
                request.getAmount()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }
    
    @PostMapping("/transfer")
    public ResponseEntity<TransactionDto> transfer(@Valid @RequestBody TransferRequest request) {
        log.info("Received transfer request from account: {} to account: {}", 
                request.getFromAccountId(), request.getToAccountId());
        
        TransactionDto transaction = transactionService.transfer(
                request.getFromAccountId(),
                request.getToAccountId(),
                request.getAmount()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TransactionDto> getTransaction(@PathVariable Long id) {
        log.info("Received request to get transaction: {}", id);
        
        TransactionDto transaction = transactionService.getTransactionById(id);
        return ResponseEntity.ok(transaction);
    }
    
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<TransactionDto>> getTransactionHistory(@PathVariable Long accountId) {
        log.info("Received request to get transaction history for account: {}", accountId);
        
        List<TransactionDto> transactions = transactionService.getTransactionHistory(accountId);
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping("/balance/{accountId}")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable Long accountId) {
        log.info("Received request to get balance for account: {}", accountId);
        
        BalanceResponse balance = transactionService.getBalance(accountId);
        return ResponseEntity.ok(balance);
    }
}
