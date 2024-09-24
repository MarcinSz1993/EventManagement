package com.marcinsz.eventmanagementsystem.mapper;

import com.marcinsz.eventmanagementsystem.model.TransactionType;
import com.marcinsz.eventmanagementsystem.request.*;

public class TransactionMapper {

    public static TransactionKafkaRequest convertBuyTicketRequestToTransactionRequest(BuyTicketRequest buyTicketRequest) {
        return TransactionKafkaRequest.builder()
                .accountNumber(buyTicketRequest.getNumberAccount())
                .amount(0.0)
                .transactionType(TransactionType.ONLINE_PAYMENT)
                .organizerBankAccountNumber(null)
                .build();
    }

    public static TransactionKafkaRequest convertBuyTicketsFromCartRequestToTransactionRequest(BuyTicketsFromCartRequest buyTicketsFromCartRequest) {
        return TransactionKafkaRequest.builder()
                .accountNumber(buyTicketsFromCartRequest.getNumberAccount())
                .amount(0.0)
                .transactionType(TransactionType.ONLINE_PAYMENT)
                .build();
    }


    public static ExecuteTransactionRequest convertTransactionRequestToExecuteTransactionRequest(TransactionKafkaRequest transactionKafkaRequest) {
        return ExecuteTransactionRequest.builder()
                .senderAccountNumber(transactionKafkaRequest.getAccountNumber())
                .amount(transactionKafkaRequest.getAmount())
                .transactionType(transactionKafkaRequest.getTransactionType())
                .recipientAccountNumber(transactionKafkaRequest.getOrganizerBankAccountNumber())
                .build();
    }

}
