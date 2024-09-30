package com.marcinsz.eventmanagementsystem.service;

import com.marcinsz.eventmanagementsystem.configuration.BankServiceConfig;
import com.marcinsz.eventmanagementsystem.exception.*;
import com.marcinsz.eventmanagementsystem.mapper.BankServiceMapper;
import com.marcinsz.eventmanagementsystem.mapper.TransactionMapper;
import com.marcinsz.eventmanagementsystem.model.Cart;
import com.marcinsz.eventmanagementsystem.model.Event;
import com.marcinsz.eventmanagementsystem.model.Ticket;
import com.marcinsz.eventmanagementsystem.model.User;
import com.marcinsz.eventmanagementsystem.repository.EventRepository;
import com.marcinsz.eventmanagementsystem.repository.TicketRepository;
import com.marcinsz.eventmanagementsystem.repository.UserRepository;
import com.marcinsz.eventmanagementsystem.request.*;
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
    private final BankServiceConfig bankServiceConfig;
    private final KafkaMessageProducer kafkaMessageProducer;

    @Transactional
    public void buyTicket(BuyTicketRequest buyTicketRequest,
                          String token) {
        BankServiceLoginRequest bankServiceLoginRequest = BankServiceMapper.convertBuyTicketRequestToBankServiceLoginRequest(buyTicketRequest);
        String username = jwtService.extractUsername(token);
        User user = userRepository.findByUsername(username).orElseThrow(() -> UserNotFoundException.forUsername(username));
        Event event = eventRepository.findById(buyTicketRequest.getEventId()).orElseThrow(() -> new EventNotFoundException(buyTicketRequest.getEventId()));
        String organizerBankAccountNumber = event.getOrganizer().getAccountNumber();
        Ticket userTicket = getAndValidateTicket(user, event);
        TransactionKafkaRequest transactionKafkaRequest = getTransactionKafkaRequestForSingleTicket(buyTicketRequest, event, user, organizerBankAccountNumber);
        String bankServiceToken = verifyUserInBankService(bankServiceLoginRequest, transactionKafkaRequest);
        prepareDataForTransaction(userTicket, transactionKafkaRequest, bankServiceToken);
    }

    @Transactional
    public void buyTicketsFromCart(BuyTicketsFromCartRequest buyTicketsFromCartRequest,
                                   String token,
                                   HttpServletRequest httpServletRequest) {
        BankServiceLoginRequest bankServiceLoginRequest = BankServiceMapper.convertBuyTicketsFromCartRequestToBankServiceLoginRequest(buyTicketsFromCartRequest);
        Cart cart = (Cart) httpServletRequest.getSession().getAttribute("cart");
        if (cart.getEvents().isEmpty()) {
            throw new EmptyCartException("Your cart is empty.");
        }
        String username = jwtService.extractUsername(token);
        User user = userRepository.findByUsername(username).orElseThrow(() -> UserNotFoundException.forUsername(username));
        Set<String> setOfEventNames = cart.getEvents().keySet();
        List<String> listOfEventNames = setOfEventNames.stream().toList();

        boolean isBankServiceAvailable = true;

        for (String eventName : listOfEventNames) {
            Event event = eventRepository.findByEventName(eventName).orElseThrow(() -> new EventNotFoundException(eventName));
            String organizerAccountNumber = event.getOrganizer().getAccountNumber();
            Ticket userTicket = getAndValidateTicket(user, event);
            TransactionKafkaRequest transactionKafkaRequest = getTransactionKafkaRequestForCart(buyTicketsFromCartRequest, event, user, organizerAccountNumber);

            try {
                String bankServiceToken = webClient.post()
                        .uri(bankServiceConfig.getUrl() + bankServiceConfig.getUserLogin())
                        .bodyValue(bankServiceLoginRequest)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
                ExecuteTransactionRequest executeTransactionRequest = TransactionMapper.convertTransactionRequestToExecuteTransactionRequest(transactionKafkaRequest);
                webClient.put()
                        .uri(bankServiceConfig.getUrl() + bankServiceConfig.getTransaction())
                        .header(HttpHeaders.AUTHORIZATION,bankServiceToken)
                        .header(HttpHeaders.ACCEPT,MediaType.APPLICATION_JSON_VALUE)
                        .header(HttpHeaders.CACHE_CONTROL,"no-cache")
                        .header(HttpHeaders.CONTENT_TYPE,MediaType.APPLICATION_JSON_VALUE)
                        .bodyValue(executeTransactionRequest)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                userTicket.setHasTicket(true);
                ticketRepository.save(userTicket);
            } catch (WebClientRequestException ex){
                kafkaMessageProducer.sendTransactionRequestMessageToExpectingPaymentsTopic(transactionKafkaRequest);
                isBankServiceAvailable = false;
            }
        }
        if (!isBankServiceAvailable) {
            throw new BankServiceServerNotAvailableException();
        }
        cart.getEvents().clear();
    }

    private TransactionKafkaRequest getTransactionKafkaRequestForCart(BuyTicketsFromCartRequest buyTicketsFromCartRequest, Event event, User user, String organizerAccountNumber) {
        TransactionKafkaRequest transactionKafkaRequest = TransactionMapper.convertBuyTicketsFromCartRequestToTransactionRequest(buyTicketsFromCartRequest);
        transactionKafkaRequest.setAmount(event.getTicketPrice());
        transactionKafkaRequest.setUserId(user.getId());
        transactionKafkaRequest.setEventId(event.getId());
        transactionKafkaRequest.setOrganizerBankAccountNumber(organizerAccountNumber);
        return transactionKafkaRequest;
    }

    private void validateTicket(Ticket userTicket) {
        if (userTicket.isHasTicket()) {
            throw new TicketAlreadyBoughtException("You have a ticket for this event.");
        }
    }

    private void prepareDataForTransaction(Ticket userTicket, TransactionKafkaRequest transactionKafkaRequest, String bankServiceToken) {
        ExecuteTransactionRequest executeTransactionRequest = TransactionMapper.convertTransactionRequestToExecuteTransactionRequest(transactionKafkaRequest);
        try {
            webClient.put()
                    .uri(bankServiceConfig.getUrl() + bankServiceConfig.getTransaction())
                    .header(HttpHeaders.AUTHORIZATION, bankServiceToken)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(executeTransactionRequest)
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
                        kafkaMessageProducer.sendTransactionRequestMessageToExpectingPaymentsTopic(transactionKafkaRequest);
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
                                           TransactionKafkaRequest transactionKafkaRequest) {
        return webClient.post()
                .uri(bankServiceConfig.getUrl() + bankServiceConfig.getUserLogin())
                .bodyValue(bankServiceLoginRequest)
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(_ -> {
                    kafkaMessageProducer.sendTransactionRequestMessageToExpectingPaymentsTopic(transactionKafkaRequest);
                    throw new BankServiceServerNotAvailableException();
                })
                .block();
    }

    private Ticket getAndValidateTicket(User user, Event event) {
        Ticket userTicket = ticketRepository.findByUser_IdAndEvent_Id(user.getId(), event.getId()).orElse(Ticket.builder()
                .hasTicket(false)
                .user(user)
                .event(event)
                .build());
        validateTicket(userTicket);
        return userTicket;
    }

    private TransactionKafkaRequest getTransactionKafkaRequestForSingleTicket(BuyTicketRequest buyTicketRequest, Event event, User user, String organizerBankAccountNumber) {
        TransactionKafkaRequest transactionKafkaRequest = TransactionMapper.convertBuyTicketRequestToTransactionRequest(buyTicketRequest);
        transactionKafkaRequest.setAmount(event.getTicketPrice());
        transactionKafkaRequest.setUserId(user.getId());
        transactionKafkaRequest.setEventId(event.getId());
        transactionKafkaRequest.setOrganizerBankAccountNumber(organizerBankAccountNumber);
        return transactionKafkaRequest;
    }
}
