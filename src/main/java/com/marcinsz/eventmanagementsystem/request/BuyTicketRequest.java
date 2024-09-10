package com.marcinsz.eventmanagementsystem.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuyTicketRequest {
    @NotBlank(message = "Event id is a must to identification the event!")
    private Long eventId;
    @NotBlank(message = "A number account is necessary.")
    private String numberAccount;
    @NotBlank(message = "A password is required")
    private String bankPassword;
}
