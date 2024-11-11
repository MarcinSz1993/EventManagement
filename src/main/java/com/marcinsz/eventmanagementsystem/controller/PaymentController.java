package com.marcinsz.eventmanagementsystem.controller;

import com.marcinsz.eventmanagementsystem.request.BuyTicketRequest;
import com.marcinsz.eventmanagementsystem.request.BuyTicketsFromCartRequest;
import com.marcinsz.eventmanagementsystem.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PutMapping("/")
    public ResponseEntity<String> buyTicket(
            @RequestHeader("Authorization") String authorizationToken,
            BuyTicketRequest buyTicketRequest) {
        String token = authorizationToken.substring("Bearer ".length());
        String result = paymentService.buyTicket(buyTicketRequest, token);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/cart")
    public ResponseEntity<String> buyTicketsFromCart(@RequestBody BuyTicketsFromCartRequest buyTicketsFromCartRequest,
                                                     HttpServletRequest servletRequest,
                                                     @CookieValue String token){
        paymentService.buyTicketsFromCart(buyTicketsFromCartRequest,token,servletRequest);
        return ResponseEntity.ok("Tickets have been purchased.");
    }
}
