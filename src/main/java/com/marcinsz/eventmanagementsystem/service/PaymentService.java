package com.marcinsz.eventmanagementsystem.service;

import com.marcinsz.eventmanagementsystem.configuration.BankServiceConfig;
import com.marcinsz.eventmanagementsystem.exception.*;
import com.marcinsz.eventmanagementsystem.mapper.BankServiceMapper;
import com.marcinsz.eventmanagementsystem.mapper.TransactionMapper;
import com.marcinsz.eventmanagementsystem.model.*;
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
    public String buyTicket(BuyTicketRequest buyTicketRequest,String token) {
        String username = extractUsernameFromToken(token);
        User user = fetchUserFromDatabase(username);
        Event event = fetchEventFromDatabase(buyTicketRequest);
        boolean ticketExists = validateTicket(user, event);
        BankServiceLoginRequest bankServiceLoginRequest = BankServiceMapper.convertBuyTicketRequestToBankServiceLoginRequest(buyTicketRequest);
        TransactionKafkaRequest transactionKafkaRequest = createTransactionKafkaRequest(buyTicketRequest, user, event);
        ExecuteTransactionRequest executeTransactionRequest = TransactionMapper.convertTransactionRequestToExecuteTransactionRequest(transactionKafkaRequest);
        executeTransactionRequest.setPassword(bankServiceLoginRequest.getPassword());

        if (ticketExists) {
            throw new TicketAlreadyBoughtException("Ticket already bought.");
        }
        try {
            String bankToken = verifyUserInBankService(bankServiceLoginRequest);
            executeTransactionInBankService(bankToken, executeTransactionRequest);
            Ticket ticket = createNewTicket(user, event);
            ticketRepository.save(ticket);
        }   catch(WebClientRequestException exception){
            log.error("WebClientRequestException", exception);
            kafkaMessageProducer.sendTransactionRequestMessageToExpectingPaymentsTopic(transactionKafkaRequest);
            throw new BankServiceServerNotAvailableException();
        }
        return "Ticket bought successfully";
    }

    private boolean validateTicket(User user, Event event) {
        return ticketRepository.existsTicketByUserAndEvent(user, event);
    }

    private Event fetchEventFromDatabase(BuyTicketRequest buyTicketRequest) {
        return eventRepository.findById(buyTicketRequest.getEventId()).orElseThrow(() -> new EventNotFoundException(buyTicketRequest.getEventId()));
    }

    private User fetchUserFromDatabase(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> UserNotFoundException.forUsername(username));
    }

    private String extractUsernameFromToken(String token) {
        return jwtService.extractUsername(token);
    }

    private Ticket createNewTicket(User user, Event event) {
        return Ticket.builder()
                .hasTicket(true)
                .user(user)
                .event(event)
                .build();
    }

    private void executeTransactionInBankService(String bankToken, ExecuteTransactionRequest executeTransactionRequest) {
        webClient.put()
                .uri("http://localhost:9090/transactions/")
                .header(HttpHeaders.AUTHORIZATION, bankToken)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.CACHE_CONTROL,"no-cache")
                .header(HttpHeaders.CONTENT_TYPE,MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(executeTransactionRequest)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(10))
                .block();
    }

    private TransactionKafkaRequest createTransactionKafkaRequest(BuyTicketRequest buyTicketRequest, User user, Event event) {
        return TransactionKafkaRequest.builder()
                .userId(user.getId())
                .eventId(buyTicketRequest.getEventId())
                .accountNumber(buyTicketRequest.getNumberAccount())
                .password(buyTicketRequest.getBankPassword())
                .amount(event.getTicketPrice())
                .organizerBankAccountNumber(event.getOrganizer().getAccountNumber())
                .transactionType(TransactionType.ONLINE_PAYMENT)
                .build();
    }


    private String verifyUserInBankService(BankServiceLoginRequest bankServiceLoginRequest) {
        return webClient.post()
                .uri("http://localhost:9090/clients/login")
                .bodyValue(bankServiceLoginRequest)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .map(_ -> new BadCredentialsForBankServiceException()))
                .bodyToMono(String.class)
                .block();
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
        String username = extractUsernameFromToken(token);
        User user = fetchUserFromDatabase(username);
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

    private Ticket getAndValidateTicket(User user, Event event) {
        Ticket userTicket = ticketRepository.findByUser_IdAndEvent_Id(user.getId(), event.getId()).orElse(Ticket.builder()
                .hasTicket(false)
                .user(user)
                .event(event)
                .build());
        validateTicket(userTicket);
        return userTicket;
    }

}
