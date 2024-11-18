package com.marcinsz.eventmanagementsystem.mapper;

import com.marcinsz.eventmanagementsystem.model.TransactionType;
import com.marcinsz.eventmanagementsystem.request.*;

public class TransactionMapper {

    public static TransactionKafkaRequest convertBuyTicketsFromCartRequestToTransactionRequest(BuyTicketsFromCartRequest buyTicketsFromCartRequest) {
        return TransactionKafkaRequest.builder()
                .accountNumber(buyTicketsFromCartRequest.getNumberAccount())
                .amount(0.0)
                .transactionType(TransactionType.ONLINE_PAYMENT)
                .build();
    }


    public static ExecuteTransactionRequest convertTransactionKafkaRequestToExecuteTransactionRequest(TransactionKafkaRequest transactionKafkaRequest) {
        return ExecuteTransactionRequest.builder()
                .senderAccountNumber(transactionKafkaRequest.getAccountNumber())
                .password(transactionKafkaRequest.getPassword())
                .amount(transactionKafkaRequest.getAmount())
                .transactionType(transactionKafkaRequest.getTransactionType())
                .recipientAccountNumber(transactionKafkaRequest.getOrganizerBankAccountNumber())
                .build();
    }

}
