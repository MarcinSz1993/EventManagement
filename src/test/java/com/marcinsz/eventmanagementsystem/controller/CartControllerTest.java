package com.marcinsz.eventmanagementsystem.controller;

import com.marcinsz.eventmanagementsystem.exception.EventNotFoundException;
import com.marcinsz.eventmanagementsystem.exception.NotExistingEventInTheCart;
import com.marcinsz.eventmanagementsystem.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CartControllerTest {

    @Mock
    private CartService cartService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private CartController cartController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void successfullyAddToCartShouldReturnCode200WithSpecifiedCommunication() {
        Long eventId = 1L;

        Mockito.doNothing().when(cartService).addToCart(eventId, httpServletRequest);

        ResponseEntity<String> response = cartController.addToCart(eventId, httpServletRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Ticket has been added to the cart.");

        Mockito.verify(cartService, Mockito.times(1)).addToCart(eventId, httpServletRequest);
    }

    @Test
    public void addToCartShouldThrowEventNotFoundExceptionWhenEventIsNotFound() {
        Long eventId = 999L;
        Mockito.doThrow(new EventNotFoundException(eventId)).when(cartService).addToCart(eventId, httpServletRequest);

        assertThrows(EventNotFoundException.class, () -> cartController.addToCart(eventId, httpServletRequest));
    }

    @Test
    public void removeTicketFromCartSuccessfullyShouldReturnCode200WithSpecificCommunicate() {
        String eventName = "Event name";
        Mockito.doNothing().when(cartService).removeFromCart(eventName, httpServletRequest);

        ResponseEntity<String> response = cartController.removeTicketFromCart(eventName, httpServletRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(String.format("Ticket for event %s has been removed from the cart.", eventName.toUpperCase()));
    }

    @Test
    public void removeTicketFromCartShouldThrowEventNotFoundExceptionWhenEventIsNotFound() {
        String eventName = "Event name";
        Mockito.doThrow(new EventNotFoundException(eventName)).when(cartService).removeFromCart(eventName, httpServletRequest);

        Assertions.assertThatThrownBy(() -> cartController.removeTicketFromCart(eventName,httpServletRequest))
                .isInstanceOf(EventNotFoundException.class)
                .hasMessage(String.format("The event with event name: %s does not exists.",eventName.toUpperCase()));
    }

    @Test
    public void removeTicketFromCartShouldThrowNotExistingEventInTheCartExceptionWhenUserTriesToRemoveNotExistingTicketInTheCart(){
        String eventName = "Not existing ticket in the cart.";

        Mockito.doThrow(new NotExistingEventInTheCart(eventName)).when(cartService).removeFromCart(eventName,httpServletRequest);

        Assertions.assertThatThrownBy(() -> cartController.removeTicketFromCart(eventName,httpServletRequest))
                .isInstanceOf(NotExistingEventInTheCart.class)
                .hasMessage(String.format("There is no event %s in the cart.", eventName));
    }
}