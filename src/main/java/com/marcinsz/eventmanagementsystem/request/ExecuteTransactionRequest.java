package com.marcinsz.eventmanagementsystem.request;

import com.marcinsz.eventmanagementsystem.model.TransactionType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExecuteTransactionRequest {
    private String senderAccountNumber;
    private double amount;
    private String password;
    private TransactionType transactionType;
    private String recipientAccountNumber;
}
