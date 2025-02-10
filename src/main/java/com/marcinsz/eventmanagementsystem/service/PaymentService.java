package com.marcinsz.eventmanagementsystem.service;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final WebClient bankServicePaymentWebClient;
    private final WebClient bankServiceLoginWebClient;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final TicketRepository ticketRepository;
    //private final KafkaMessageProducer kafkaMessageProducer;

    @Transactional
    public String buyTicket(BuyTicketRequest buyTicketRequest, String token) {
        String username = extractUsernameFromToken(token);
        User user = fetchUserFromDatabase(username);
        Event event = fetchEventFromDatabase(buyTicketRequest);
        boolean ticketExists = validateTicket(user, event);
        BankServiceLoginRequest bankServiceLoginRequest = BankServiceMapper.convertBuyTicketRequestToBankServiceLoginRequest(buyTicketRequest);
        TransactionKafkaRequest transactionKafkaRequest = createTransactionKafkaRequest(buyTicketRequest, user, event);
        ExecuteTransactionRequest executeTransactionRequest = TransactionMapper.convertTransactionKafkaRequestToExecuteTransactionRequest(transactionKafkaRequest);
        executeTransactionRequest.setPassword(bankServiceLoginRequest.getPassword());

        if (ticketExists) {
            throw new TicketAlreadyBoughtException("Ticket already bought.");
        }
        try {
            String bankToken = verifyUserInBankService(bankServiceLoginRequest);
            executeTransactionInBankService(bankToken, executeTransactionRequest);
            Ticket ticket = createNewTicket(user, event);
            ticketRepository.save(ticket);
        } catch (WebClientRequestException exception) {
            log.error("WebClientRequestException", exception);
            //kafkaMessageProducer.sendTransactionRequestMessageToExpectingPaymentsTopic(transactionKafkaRequest);
            throw new BankServiceServerNotAvailableException();
        }
        return "Ticket bought successfully";
    }

    @Transactional
    public String buyTicketFromCart(BuyTicketsFromCartRequest buyTicketRequest
            , HttpServletRequest httpServletRequest) {
        String headerAuthorization = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);
        String userToken = headerAuthorization.substring("Bearer ".length());
        String username = extractUsernameFromToken(userToken);
        User user = fetchUserFromDatabase(username);
        String bankToken;
        try {
            BankServiceLoginRequest bankServiceLoginRequest = BankServiceMapper.convertBuyTicketsFromCartRequestToBankServiceLoginRequest(buyTicketRequest);
            bankToken = verifyUserInBankService(bankServiceLoginRequest);
        } catch (WebClientRequestException exception) {
            log.error("Mamy błąd!");
            throw new BankServiceServerNotAvailableException();
        }

        Object userCartFromSession = httpServletRequest.getSession().getAttribute("cart");
        Cart cart = (Cart) userCartFromSession;

        if (cart.getEvents().isEmpty()) {
            throw new EmptyCartException("Empty cart.");
        }
        List<String> listOfEventNames = cart.getEvents().keySet()
                .stream()
                .toList();
        for (String eventName : listOfEventNames) {
            Event event = eventRepository.findByEventName(eventName).orElseThrow(() -> new EventNotFoundException(eventName));
            ticketValidation(user, event);
            String recipientAccountNumber = event.getOrganizer().getAccountNumber();
            ExecuteTransactionRequest executeTransactionRequest = createExecuteTransactionRequest(buyTicketRequest, recipientAccountNumber, event);
            executeTransactionInBankService(bankToken, executeTransactionRequest);
            Ticket ticket = createNewTicket(user, event);
            ticketRepository.save(ticket);
            cart.removeTicket(event);
        }
        return "Tickets have been purchased successfully.";
    }

    private ExecuteTransactionRequest createExecuteTransactionRequest(BuyTicketsFromCartRequest buyTicketRequest, String recipientAccountNumber, Event event) {
        return ExecuteTransactionRequest.builder()
                .password(buyTicketRequest.getBankPassword())
                .senderAccountNumber(buyTicketRequest.getNumberAccount())
                .recipientAccountNumber(recipientAccountNumber)
                .amount(event.getTicketPrice())
                .transactionType(TransactionType.ONLINE_PAYMENT)
                .build();
    }

    private Ticket createNewTicket(User user, Event event) {
        return Ticket.builder()
                .hasTicket(true)
                .user(user)
                .event(event)
                .build();
    }

    protected void executeTransactionInBankService(String bankToken, ExecuteTransactionRequest executeTransactionRequest) {
        bankServicePaymentWebClient.put()
                .header(HttpHeaders.AUTHORIZATION, bankToken)
                .bodyValue(executeTransactionRequest)
                .retrieve()
                .onStatus(httpStatusCode -> httpStatusCode.value() == 402,
                        clientResponse -> clientResponse.bodyToMono(String.class)
                        .map((Function<String, Throwable>) _ -> new NotEnoughMoneyException()))

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

    protected String verifyUserInBankService(BankServiceLoginRequest bankServiceLoginRequest) {

        return bankServiceLoginWebClient.post()
                .bodyValue(bankServiceLoginRequest)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .map(_ -> new BadCredentialsForBankServiceException()))
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(10))
                .block();
    }

    private void ticketValidation(User user, Event event) {
        if (ticketRepository.findByUser_IdAndEvent_Id(user.getId(), event.getId()).isPresent()) {
            throw new TicketAlreadyBoughtException(String.format("You already bought a ticket for event %s", event.getEventName()));

        }
    }

    protected boolean validateTicket(User user, Event event) {
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
}
