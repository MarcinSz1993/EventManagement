package com.marcinsz.eventmanagementsystem.controller;

import com.marcinsz.eventmanagementsystem.model.Cart;
import com.marcinsz.eventmanagementsystem.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> addToCart(@RequestParam Long eventId,
                                            HttpServletRequest httpServletRequest) {
            cartService.addToCart(eventId, httpServletRequest);

            return ResponseEntity.ok("Ticket has been added to the cart.");
    }

    @GetMapping
    public ResponseEntity<Cart> getCart(HttpServletRequest httpServletRequest) {
        Cart cart = cartService.showCart(httpServletRequest);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping
    public ResponseEntity<String> removeTicketFromCart(String eventName,
                                                       HttpServletRequest httpServletRequest) {
        cartService.removeFromCart(eventName, httpServletRequest);
        //return ResponseEntity.ok().body("Ticket for event " + eventName.toUpperCase() + " has been removed from the cart.");
        return ResponseEntity.ok().body(String.format("Ticket for event %s has been removed from the cart.", eventName.toUpperCase()));
    }
}
