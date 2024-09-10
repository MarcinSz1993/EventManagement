package com.marcinsz.eventmanagementsystem.service;

import com.marcinsz.eventmanagementsystem.exception.EventNotFoundException;
import com.marcinsz.eventmanagementsystem.model.Cart;
import com.marcinsz.eventmanagementsystem.model.Event;
import com.marcinsz.eventmanagementsystem.repository.EventRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class CartService {

    private final EventRepository eventRepository;

    public void addToCart(Long eventId, HttpServletRequest httpServletRequest) {
        if(httpServletRequest.getSession().getAttribute("cart") == null) {
            httpServletRequest.getSession().setAttribute("cart", new Cart(new HashMap<>()));
        }
        Cart cart = getCart(httpServletRequest);
        Event event = getEvent(eventId);
        cart.addTicket(event);
    }

    public void removeFromCart(String eventName, HttpServletRequest httpServletRequest) {
        Cart cart = getCart(httpServletRequest);
        Event event = eventRepository.findByEventName(eventName).orElseThrow(() -> new EventNotFoundException(eventName));
        cart.removeTicket(event);
    }

    public Cart showCart(HttpServletRequest httpServletRequest) {
        return getCart(httpServletRequest);
    }

    private Event getEvent(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));
    }

    private Cart getCart(HttpServletRequest httpServletRequest) {
        String CART_SESSION_ATTRIBUTE = "cart";
        return (Cart) httpServletRequest.getSession().getAttribute(CART_SESSION_ATTRIBUTE);
    }
}
