package com.marcinsz.eventmanagementsystem.service;

import com.marcinsz.eventmanagementsystem.exception.*;
import com.marcinsz.eventmanagementsystem.mapper.RequestMapper;
import com.marcinsz.eventmanagementsystem.model.Cart;
import com.marcinsz.eventmanagementsystem.model.Event;
import com.marcinsz.eventmanagementsystem.model.Ticket;
import com.marcinsz.eventmanagementsystem.model.User;
import com.marcinsz.eventmanagementsystem.repository.EventRepository;
import com.marcinsz.eventmanagementsystem.repository.TicketRepository;
import com.marcinsz.eventmanagementsystem.repository.UserRepository;
import com.marcinsz.eventmanagementsystem.request.BankServiceLoginRequest;
import com.marcinsz.eventmanagementsystem.request.BuyTicketRequest;
import com.marcinsz.eventmanagementsystem.request.BuyTicketsFromCartRequest;
import com.marcinsz.eventmanagementsystem.request.TransactionRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final WebClient webClient;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final TicketRepository ticketRepository;
    private final KafkaMessageProducer kafkaMessageProducer;

    @Transactional
    public void buyTicket(BuyTicketRequest buyTicketRequest,
                          String token) {
        BankServiceLoginRequest bankServiceLoginRequest = RequestMapper.convertBuyTicketRequestToBankServiceLoginRequest(buyTicketRequest);
        String username = jwtService.extractUsername(token);
        User user = userRepository.findByUsername(username).orElseThrow(() -> UserNotFoundException.forUsername(username));
        Event event = eventRepository.findById(buyTicketRequest.getEventId()).orElseThrow(() -> new EventNotFoundException(buyTicketRequest.getEventId()));
        Ticket userTicket = ticketRepository.findByUser_IdAndEvent_Id(user.getId(), event.getId()).orElse(Ticket.builder()
                .hasTicket(false)
                .user(user)
                .event(event)
                .build());
        validateTicket(userTicket);
        TransactionRequest transactionRequest = RequestMapper.convertBuyTicketRequestToTransactionRequest(buyTicketRequest);
        transactionRequest.setAmount(event.getTicketPrice());
        transactionRequest.setUserId(user.getId());
        transactionRequest.setEventId(event.getId());
        String bankServiceToken = verifyUserInBankService(bankServiceLoginRequest,transactionRequest);
        prepareDataForTransaction(userTicket, transactionRequest, bankServiceToken);
    }

    @Transactional
    public void buyTicketsFromCart(BuyTicketsFromCartRequest buyTicketsFromCartRequest,
                                   String token,
                                   HttpServletRequest httpServletRequest) {
        BankServiceLoginRequest bankServiceLoginRequest = RequestMapper.convertBuyTicketsFromCartRequestToBankServiceLoginRequest(buyTicketsFromCartRequest);
        Cart cart = (Cart) httpServletRequest.getSession().getAttribute("cart");
        if (cart.getEvents().isEmpty()) {
            throw new EmptyCartException("Your cart is empty.");
        }
        String username = jwtService.extractUsername(token);
        User user = userRepository.findByUsername(username).orElseThrow(() -> UserNotFoundException.forUsername(username));
        Set<String> setOfEventNames = cart.getEvents().keySet();
        List<String> listOfEventNames = setOfEventNames.stream().toList();

        for (String eventName : listOfEventNames) {
            Ticket userTicket = ticketRepository.findByUser_IdAndEvent_EventName(user.getId(), eventName)
                    .orElse(Ticket
                            .builder()
                            .hasTicket(false)
                            .user(user)
                            .event(eventRepository.findByEventName(eventName).orElseThrow(() -> new EventNotFoundException(eventName)))
                            .build());
            validateTicket(userTicket);
            TransactionRequest transactionRequest = RequestMapper.convertbuyTicketsFromCartRequestToTransactionRequest(buyTicketsFromCartRequest);
            transactionRequest.setAmount(cart.getTotalPrice());
            String bankServiceToken = verifyUserInBankService(bankServiceLoginRequest,transactionRequest);
            prepareDataForTransaction(userTicket, transactionRequest, bankServiceToken);
            cart.getEvents().remove(eventName);
        }
    }

    private void validateTicket(Ticket userTicket) {
        if (userTicket.isHasTicket()) {
            throw new TicketAlreadyBoughtException("You have a ticket for this event.");
        }
    }

    private void prepareDataForTransaction(Ticket userTicket, TransactionRequest transactionRequest, String bankServiceToken) {
        try {
            webClient.put()
                    .uri("http://localhost:9090/transactions/")
                    .header(HttpHeaders.AUTHORIZATION, bankServiceToken)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(transactionRequest)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, response -> {
                        log.error("Client error while processing transaction: {}", response.statusCode());
                        return Mono.error(new TransactionProcessClientException("Client error while processing transaction."));
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, response -> {
                        log.error("Server error while processing transaction: {}", response.statusCode());
                        return Mono.error(new TransactionProcessServerException("Server error while processing transaction."));
                    })
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(15))
                    .doOnError(WebClientRequestException.class, ex -> {
                        log.error("Error during WebClient request: {}", ex.getMessage());
                        kafkaMessageProducer.sendTransactionRequestMessageToExpectingPaymentsTopic(transactionRequest);
                        throw new BankServiceServerNotAvailableException();
                    })
                    .block();
        } catch (Exception ex) {
            log.error("Exception in prepareDataForTransaction: {}", ex.getMessage());
            throw ex;
        }
        userTicket.setHasTicket(true);
        ticketRepository.save(userTicket);
    }

    private String verifyUserInBankService(BankServiceLoginRequest bankServiceLoginRequest,
                                           TransactionRequest transactionRequest) {
        return webClient.post()
                .uri("http://localhost:9090/clients/login")
                .bodyValue(bankServiceLoginRequest)
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(_ -> {
                    kafkaMessageProducer.sendTransactionRequestMessageToExpectingPaymentsTopic(transactionRequest);
                    throw new BankServiceServerNotAvailableException();
                })
                .block();
    }
}
