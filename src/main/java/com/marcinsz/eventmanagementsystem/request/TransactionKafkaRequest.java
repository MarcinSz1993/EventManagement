package com.marcinsz.eventmanagementsystem.request;

import com.marcinsz.eventmanagementsystem.model.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionKafkaRequest {
    private String accountNumber;
    private double amount;
    private TransactionType transactionType;
    private Long eventId;
    private Long userId;
    private String organizerBankAccountNumber;
}
