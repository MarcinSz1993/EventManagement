package com.marcinsz.eventmanagementsystem.mapper;

import com.marcinsz.eventmanagementsystem.model.TransactionType;
import com.marcinsz.eventmanagementsystem.request.BankServiceLoginRequest;
import com.marcinsz.eventmanagementsystem.request.BuyTicketRequest;
import com.marcinsz.eventmanagementsystem.request.BuyTicketsFromCartRequest;
import com.marcinsz.eventmanagementsystem.request.TransactionRequest;

public class RequestMapper {

    public static TransactionRequest convertBuyTicketRequestToTransactionRequest(BuyTicketRequest buyTicketRequest) {
        return TransactionRequest.builder()
                .accountNumber(buyTicketRequest.getNumberAccount())
                .amount(0.0)
                .transactionType(TransactionType.ONLINE_PAYMENT)
                .build();
    }

    public static TransactionRequest convertbuyTicketsFromCartRequestToTransactionRequest(BuyTicketsFromCartRequest buyTicketsFromCartRequest) {
        return TransactionRequest.builder()
                .accountNumber(buyTicketsFromCartRequest.getNumberAccount())
                .amount(0.0)
                .transactionType(TransactionType.ONLINE_PAYMENT)
                .build();
    }

    public static BankServiceLoginRequest convertBuyTicketRequestToBankServiceLoginRequest(BuyTicketRequest buyTicketRequest) {
        return BankServiceLoginRequest.builder()
                .accountNumber(buyTicketRequest.getNumberAccount())
                .password(buyTicketRequest.getBankPassword())
                .build();
    }

    public static BankServiceLoginRequest convertBuyTicketsFromCartRequestToBankServiceLoginRequest(BuyTicketsFromCartRequest buyTicketsFromCartRequest) {
        return BankServiceLoginRequest.builder()
                .accountNumber(buyTicketsFromCartRequest.getNumberAccount())
                .password(buyTicketsFromCartRequest.getBankPassword())
                .build();
    }

}
