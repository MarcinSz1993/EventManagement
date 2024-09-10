package com.marcinsz.eventmanagementsystem.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BankServiceLoginRequest {
    private String accountNumber;
    private String password;
}
