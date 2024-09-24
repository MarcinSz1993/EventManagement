package com.marcinsz.eventmanagementsystem.mapper;

import com.marcinsz.eventmanagementsystem.request.BankServiceLoginRequest;
import com.marcinsz.eventmanagementsystem.request.BuyTicketRequest;
import com.marcinsz.eventmanagementsystem.request.BuyTicketsFromCartRequest;

public class BankServiceMapper {
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
