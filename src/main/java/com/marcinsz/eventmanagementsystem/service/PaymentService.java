package com.marcinsz.eventmanagementsystem.service;

import com.marcinsz.eventmanagementsystem.exception.*;
import com.marcinsz.eventmanagementsystem.mapper.RequestMapper;
import com.marcinsz.eventmanagementsystem.model.Event;
import com.marcinsz.eventmanagementsystem.model.Ticket;
import com.marcinsz.eventmanagementsystem.model.User;
import com.marcinsz.eventmanagementsystem.repository.EventRepository;
import com.marcinsz.eventmanagementsystem.repository.TicketRepository;
import com.marcinsz.eventmanagementsystem.repository.UserRepository;
import com.marcinsz.eventmanagementsystem.request.BuyTicketRequest;
import com.marcinsz.eventmanagementsystem.request.TransactionRequest;
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
    public void buyTicket(BuyTicketRequest buyTicketRequest, String token) {
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

        webClient.put()
                .uri("http://localhost:9090/transactions/")
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(transactionRequest)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, _ -> Mono.error(new TransactionProcessClientException("Client error while processing transaction.")))
                .onStatus(HttpStatusCode::is5xxServerError, _ -> Mono.error(new TransactionProcessServerException("Server error while processing transaction.")))
                .bodyToMono(String.class)
                .doOnError(WebClientRequestException.class, _ -> {
                    throw new BankServiceServerNotAvailableException();
                })
                .block();

        userTicket.setHasTicket(true);
        ticketRepository.save(userTicket);
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
}
