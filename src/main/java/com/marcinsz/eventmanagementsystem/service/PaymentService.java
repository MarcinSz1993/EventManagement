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
import com.marcinsz.eventmanagementsystem.request.BuyTicketRequest;
import com.marcinsz.eventmanagementsystem.request.BuyTicketsFromCartRequest;
import com.marcinsz.eventmanagementsystem.request.TransactionRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final WebClient webClient;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final TicketRepository ticketRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void buyTicket(BuyTicketRequest buyTicketRequest,
                          String token) {
        String username = jwtService.extractUsername(token);
        User user = userRepository.findByUsername(username).orElseThrow(() -> UserNotFoundException.forUsername(username));
        validatePassword(buyTicketRequest, user);
        Event event = eventRepository.findById(buyTicketRequest.getEventId()).orElseThrow(() -> new EventNotFoundException(buyTicketRequest.getEventId()));
        Ticket userTicket = ticketRepository.findByUser_IdAndEvent_Id(user.getId(), event.getId()).orElse(Ticket.builder()
                .hasTicket(false)
                .user(user)
                .event(event)
                .build());
        validateTicket(userTicket);
        TransactionRequest transactionRequest = RequestMapper.convertBuyTicketRequestToTransactionRequest(buyTicketRequest);
        transactionRequest.setAmount(event.getTicketPrice());
        prepareDataForTransaction(userTicket, transactionRequest);
    }

    @Transactional
    public void buyTicketsFromCart(BuyTicketsFromCartRequest buyTicketsFromCartRequest,
                                   String token,
                                   HttpServletRequest httpServletRequest){
        Cart cart = (Cart) httpServletRequest.getSession().getAttribute("cart");
        if (cart.getEvents().isEmpty()){
            throw new EmptyCartException("Your cart is empty.");
        }
        String username = jwtService.extractUsername(token);
        User user = userRepository.findByUsername(username).orElseThrow(() -> UserNotFoundException.forUsername(username));
        validatePassword(new BuyTicketRequest(null,
                buyTicketsFromCartRequest.getNumberAccount(),
                buyTicketsFromCartRequest.getPassword()), user);
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

            prepareDataForTransaction(userTicket, transactionRequest);
            cart.getEvents().remove(eventName);
        }
    }

    private void validatePassword(BuyTicketRequest buyTicketRequest, User user) {
        if (!passwordEncoder.matches(buyTicketRequest.getPassword(), user.getPassword())) {
            throw new BadCredentialsException();
        }
    }

    private void validateTicket(Ticket userTicket) {
        if (userTicket.isHasTicket()) {
            throw new TicketAlreadyBoughtException("You have a ticket for this event.");
        }
    }

    private void prepareDataForTransaction(Ticket userTicket, TransactionRequest transactionRequest) {
        webClient.put()
                .uri("http://localhost:9090/transactions/")
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.CACHE_CONTROL,"no-cache")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(transactionRequest)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, _ -> Mono.error(new TransactionProcessClientException("Client error while processing transaction.")))
                .onStatus(HttpStatusCode::is5xxServerError, _ -> Mono.error(new TransactionProcessServerException("Server error while processing transaction.")))
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(15))
                .doOnError(WebClientRequestException.class, _ -> {
                    throw new BankServiceServerNotAvailableException();
                })
                .block();
        userTicket.setHasTicket(true);
        ticketRepository.save(userTicket);
    }
}
