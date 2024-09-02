package com.marcinsz.eventmanagementsystem.controller;

import com.marcinsz.eventmanagementsystem.request.BuyTicketRequest;
import com.marcinsz.eventmanagementsystem.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PutMapping("/")
    public ResponseEntity<String> buyTicket(BuyTicketRequest buyTicketRequest,@CookieValue String token) {
        paymentService.buyTicket(buyTicketRequest,token);
        return ResponseEntity.ok("Congratulations! You have successfully buy a ticket!");
    }
}
