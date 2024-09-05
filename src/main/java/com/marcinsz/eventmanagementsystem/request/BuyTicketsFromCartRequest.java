package com.marcinsz.eventmanagementsystem.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BuyTicketsFromCartRequest {
    private String numberAccount;
    private String password;
}
