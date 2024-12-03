package com.marcinsz.eventmanagementsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcinsz.eventmanagementsystem.exception.TicketAlreadyBoughtException;
import com.marcinsz.eventmanagementsystem.request.BuyTicketRequest;
import com.marcinsz.eventmanagementsystem.service.KafkaMessageListener;
import com.marcinsz.eventmanagementsystem.service.NotificationService;
import com.marcinsz.eventmanagementsystem.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@MockBean(KafkaMessageListener.class)
@MockBean(NotificationService.class)
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void buyTicketShouldHandleTicketAlreadyBoughtException() throws Exception {
        BuyTicketRequest buyTicketRequest = createBuyTicketRequest();
        Mockito.when(paymentService.buyTicket(buyTicketRequest,"passwordToken"))
                .thenThrow(new TicketAlreadyBoughtException("Ticket already bought."));

        mockMvc.perform(put("/api/payments/")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer passwordToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buyTicketRequest)))
                .andExpect(status().is(409))
                .andExpect(content().string("Ticket already bought."));
    }

    @Test
    public void buyTicketSuccessfully() throws Exception {
        BuyTicketRequest buyTicketRequest = createBuyTicketRequest();
        Mockito.when(paymentService.buyTicket(buyTicketRequest, "passwordToken"))
                .thenReturn("Ticket bought successfully");

        mockMvc.perform(put("/api/payments/")
                        .header("Authorization", "Bearer passwordToken")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(buyTicketRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("Ticket bought successfully"));
    }

    BuyTicketRequest createBuyTicketRequest(){
        return BuyTicketRequest.builder()
                .eventId(1L)
                .numberAccount("1234567890")
                .bankPassword("qwerty")
                .build();
    }
}