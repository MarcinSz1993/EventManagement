package com.marcinsz.eventmanagementsystem.controller;

import com.marcinsz.eventmanagementsystem.exception.BankServiceServerNotAvailableException;
import com.marcinsz.eventmanagementsystem.exception.EmptyCartException;
import com.marcinsz.eventmanagementsystem.exception.TicketAlreadyBoughtException;
import com.marcinsz.eventmanagementsystem.request.BuyTicketRequest;
import com.marcinsz.eventmanagementsystem.request.BuyTicketsFromCartRequest;
import com.marcinsz.eventmanagementsystem.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private PaymentController paymentController;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void buyTicketsFromCartShouldHandleBankServiceNotAvailableExceptionWhenTriesToMakeTransactionWhileBankServerOfflineIs(){
        BuyTicketsFromCartRequest buyTicketsFromCartRequest = buyTicketsFromCartRequest();
        String token = "token";

        Mockito.doThrow(BankServiceServerNotAvailableException.class).when(paymentService).buyTicketsFromCart(buyTicketsFromCartRequest,token,httpServletRequest);

        assertThrows(BankServiceServerNotAvailableException.class,() -> paymentController.buyTicketsFromCart(buyTicketsFromCartRequest,httpServletRequest,token));
    }

    @Test
    public void buyTicketFromCartShouldHandleEmptyCartExceptionWhenCartIsEmpty(){
        BuyTicketsFromCartRequest buyTicketsFromCartRequest = buyTicketsFromCartRequest();
        String token = "token";

        Mockito.doThrow(new EmptyCartException("Your cart is empty.")).when(paymentService).buyTicketsFromCart(buyTicketsFromCartRequest,token,httpServletRequest);

        assertThrows(EmptyCartException.class, () -> paymentController.buyTicketsFromCart(buyTicketsFromCartRequest, httpServletRequest, token));
        Mockito.verify(paymentService,Mockito.times(1)).buyTicketsFromCart(buyTicketsFromCartRequest,token,httpServletRequest);
    }

    @Test
    public void buyTicketsFromCartSuccessfully(){
        BuyTicketsFromCartRequest buyTicketsFromCartRequest = buyTicketsFromCartRequest();
        String token = "token";
        Mockito.doNothing().when(paymentService).buyTicketsFromCart(buyTicketsFromCartRequest,token,httpServletRequest);

        ResponseEntity<String> response = paymentController.buyTicketsFromCart(buyTicketsFromCartRequest, httpServletRequest, token);

        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(200);
        Assertions.assertThat(response.getBody()).isEqualTo("Tickets have been purchased.");

        Mockito.verify(paymentService,Mockito.times(1)).buyTicketsFromCart(buyTicketsFromCartRequest,token,httpServletRequest);
    }

    @Test
    public void buyTicketShouldHandleTicketAlreadyBoughtExceptionWhenUserTriesToBuyTheTicketTwice(){
        BuyTicketRequest buyTicketRequest = createTestBuyTicketRequest();
        String token = "token";
        Mockito.doThrow(TicketAlreadyBoughtException.class).when(paymentService).buyTicket(buyTicketRequest,token);

        assertThrows(TicketAlreadyBoughtException.class,() -> paymentController.buyTicket(token,buyTicketRequest));
    }

    @Test
    public void buyTicketShouldHandleBankServiceNotAvailableExceptionWhenTriesToMakeTransactionWhileBankServerOfflineIs() {
        BuyTicketRequest buyTicketRequest = createTestBuyTicketRequest();
        String token = "token";
        Mockito.doThrow(BankServiceServerNotAvailableException.class).when(paymentService).buyTicket(buyTicketRequest,token);

        Assertions.assertThatThrownBy(() -> paymentController.buyTicket(token,buyTicketRequest))
                .isInstanceOf(BankServiceServerNotAvailableException.class);


    }@Test
    public void successfullyBuyTicketShouldReturnCode200WithSpecifiedCommunicate(){
        BuyTicketRequest buyTicketRequest = createTestBuyTicketRequest();
        String token = "token";
        Mockito.doNothing().when(paymentService).buyTicket(buyTicketRequest,token);
        ResponseEntity<String> response = paymentController.buyTicket(token,buyTicketRequest);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Congratulations! You have successfully buy a ticket!",response.getBody());

        Mockito.verify(paymentService,Mockito.times(1)).buyTicket(buyTicketRequest,token);
    }

    private BuyTicketsFromCartRequest buyTicketsFromCartRequest(){
        return BuyTicketsFromCartRequest.builder()
                .numberAccount("1111111111")
                .bankPassword("qwerty")
                .build();
    }

    private BuyTicketRequest createTestBuyTicketRequest(){
        return BuyTicketRequest.builder()
                .eventId(1L)
                .numberAccount("1111111111")
                .bankPassword("qwerty")
                .build();
    }
}