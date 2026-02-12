package com.example.Transaction.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BalanceResponse {

    private Long accountId;

    private BigDecimal balance;

    private BigDecimal totalDeposits;

    private BigDecimal totalWithdrawals;

    private BigDecimal totalTransfersIn;

    private BigDecimal totalTransfersOut;

    private int totalTransactions;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime calculatedAt;
}
