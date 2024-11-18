package com.marcinsz.eventmanagementsystem.service;

import com.marcinsz.eventmanagementsystem.configuration.BankServiceConfig;
import com.marcinsz.eventmanagementsystem.exception.*;
import com.marcinsz.eventmanagementsystem.mapper.TransactionMapper;
import com.marcinsz.eventmanagementsystem.model.*;
import com.marcinsz.eventmanagementsystem.repository.EventRepository;
import com.marcinsz.eventmanagementsystem.repository.TicketRepository;
import com.marcinsz.eventmanagementsystem.repository.UserRepository;
import com.marcinsz.eventmanagementsystem.request.BankServiceLoginRequest;
import com.marcinsz.eventmanagementsystem.request.BuyTicketRequest;
import com.marcinsz.eventmanagementsystem.request.ExecuteTransactionRequest;
import com.marcinsz.eventmanagementsystem.request.TransactionKafkaRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

public class PaymentServiceTest {

    private MockWebServer mockWebServer;
    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private BankServiceConfig bankServiceConfig;

    @Mock
    private HttpSession httpSession;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private KafkaMessageProducer kafkaMessageProducer;

    @InjectMocks
    private PaymentService paymentService;

    @BeforeEach
    void setup() throws IOException {
        MockitoAnnotations.openMocks(this);
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("/").toString();
        Mockito.when(bankServiceConfig.getUrl()).thenReturn(baseUrl);
        Mockito.when(bankServiceConfig.getTransaction()).thenReturn("transactions/");
        Mockito.when(bankServiceConfig.getUserLogin()).thenReturn("clients/login");

        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();

        paymentService = new PaymentService(webClient,
                jwtService,
                userRepository,
                eventRepository,
                ticketRepository,
                bankServiceConfig,
                kafkaMessageProducer
        );
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void buyTicketShouldSendMessageToKafkaWhenBankServiceIsOffline() throws IOException {
        BuyTicketRequest buyTicketRequest = createBuyTicketRequest();
        User user = createTestUser();
        Event event = createTestEvent(user);
        TransactionKafkaRequest transactionKafkaRequest = createTransactionKafkaRequest(buyTicketRequest, user, event);
        String token = "token";
        String username = user.getUsername();

        Mockito.when(jwtService.extractUsername(token)).thenReturn(username);
        Mockito.when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        Mockito.when(eventRepository.findById(buyTicketRequest.getEventId())).thenReturn(Optional.of(event));
        Mockito.doNothing().when(kafkaMessageProducer).sendTransactionRequestMessageToExpectingPaymentsTopic(transactionKafkaRequest);

        mockWebServer.shutdown();

        Assertions.assertThrows(BankServiceServerNotAvailableException.class, () -> paymentService.buyTicket(buyTicketRequest, token));

        ArgumentCaptor<TransactionKafkaRequest> kafkaRequestArgumentCaptor = ArgumentCaptor.forClass(TransactionKafkaRequest.class);
        Mockito.verify(kafkaMessageProducer, Mockito.times(1)).sendTransactionRequestMessageToExpectingPaymentsTopic(kafkaRequestArgumentCaptor.capture());

        Assertions.assertEquals(transactionKafkaRequest, kafkaRequestArgumentCaptor.getValue());
    }

    @Test
    public void buyTicketShouldThrowNotEnoughMoneyExceptionWhenUserDoesNotHaveEnoughMoneyToBuyTheTicket() {
        BuyTicketRequest buyTicketRequest = createBuyTicketRequest();
        User user = createTestUser();
        Event event = createTestEvent(user);
        String token = "token";
        String bankToken = "bankToken";
        String username = user.getUsername();

        Mockito.when(jwtService.extractUsername(token)).thenReturn(username);
        Mockito.when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        Mockito.when(eventRepository.findById(buyTicketRequest.getEventId())).thenReturn(Optional.of(event));

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(bankToken));

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(402)
                .setBody("Not enough money on your bank account to execute the transaction."));

        NotEnoughMoneyException notEnoughMoneyException = Assertions.assertThrows(NotEnoughMoneyException.class, () -> paymentService.buyTicket(buyTicketRequest, token));
        Assertions.assertEquals("Not enough money on your bank account to execute the transaction.", notEnoughMoneyException.getMessage());

        Mockito.verify(jwtService, Mockito.times(1)).extractUsername(token);
        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(username);
        Mockito.verify(eventRepository, Mockito.times(1)).findById(buyTicketRequest.getEventId());
    }

    @Test
    public void buyTicketShouldThrowBankServiceNotAvailableExceptionWithSpecifiedCommunicateWhenBankServiceIsOffline() throws IOException {
        BuyTicketRequest buyTicketRequest = createBuyTicketRequest();
        User user = createTestUser();
        Event event = createTestEvent(user);
        String token = "token";
        String username = user.getUsername();

        Mockito.when(jwtService.extractUsername(token)).thenReturn(username);
        Mockito.when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        Mockito.when(eventRepository.findById(buyTicketRequest.getEventId())).thenReturn(Optional.of(event));

        mockWebServer.shutdown();
        BankServiceServerNotAvailableException bankServiceServerNotAvailableException = Assertions.assertThrows(BankServiceServerNotAvailableException.class, () -> paymentService.buyTicket(buyTicketRequest, token));
        Assertions.assertEquals("Bank Service server is not available", bankServiceServerNotAvailableException.getMessage());
    }

    @Test
    public void buyTicketShouldThrowBadCredentialsForBankExceptionsWhenUserTypesWrongAccountNumberOrPassword() throws InterruptedException {
        BuyTicketRequest buyTicketRequest = createBuyTicketRequest();
        User user = createTestUser();
        Event event = createTestEvent(user);
        String token = "token";
        String username = user.getUsername();

        Mockito.when(jwtService.extractUsername(token)).thenReturn(username);
        Mockito.when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        Mockito.when(eventRepository.findById(buyTicketRequest.getEventId())).thenReturn(Optional.of(event));

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setBody("You typed incorrect bank account number or password!"));

        BadCredentialsForBankServiceException badCredentialsForBankServiceException = Assertions.assertThrows(BadCredentialsForBankServiceException.class, () -> paymentService.buyTicket(buyTicketRequest, token));
        Assertions.assertEquals("You typed incorrect bank account number or password!", badCredentialsForBankServiceException.getMessage());

        RecordedRequest loginRequest = mockWebServer.takeRequest();
        Assertions.assertEquals("POST", loginRequest.getMethod());
        Assertions.assertEquals("/clients/login", loginRequest.getPath());

        Mockito.verify(jwtService, Mockito.times(1)).extractUsername(token);
        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(username);
        Mockito.verify(eventRepository, Mockito.times(1)).findById(buyTicketRequest.getEventId());
    }

    @Test
    public void buyTicketShouldThrowTicketAlreadyBoughtExceptionWithSpecifiedCommunicationWhenUserWantsToBuyTheSameTicketAgain() {
        BuyTicketRequest buyTicketRequest = createBuyTicketRequest();
        String token = "token";
        User user = createTestUser();
        String username = user.getUsername();
        Event event = createTestEvent(user);

        Mockito.when(jwtService.extractUsername(token)).thenReturn(username);
        Mockito.when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        Mockito.when(eventRepository.findById(createBuyTicketRequest().getEventId())).thenReturn(Optional.of(event));
        Mockito.when(paymentService.validateTicket(user, event)).thenReturn(true);

        TicketAlreadyBoughtException ticketAlreadyBoughtException = Assertions.assertThrows(TicketAlreadyBoughtException.class, () -> paymentService.buyTicket(buyTicketRequest, token));
        Assertions.assertEquals("Ticket already bought.", ticketAlreadyBoughtException.getMessage());

        Mockito.verify(jwtService, Mockito.times(1)).extractUsername(token);
        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(username);
        Mockito.verify(eventRepository, Mockito.times(1)).findById(createBuyTicketRequest().getEventId());
    }

    @Test
    public void buyTicketShouldThrowUserNotFoundExceptionWithSpecifiedCommunicateWhenUsernameDoesNotExistInDatabase() {
        BuyTicketRequest buyTicketRequest = createBuyTicketRequest();
        String token = "token";
        String username = "Not existing username";
        Mockito.when(jwtService.extractUsername(token)).thenReturn(username);
        Mockito.when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        UserNotFoundException userNotFoundException = Assertions.assertThrows(UserNotFoundException.class, () -> paymentService.buyTicket(buyTicketRequest, token));
        Assertions.assertEquals("There is no user with username: Not existing username", userNotFoundException.getMessage());
        Mockito.verify(jwtService, Mockito.times(1)).extractUsername(token);
        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(username);
    }

    @Test
    public void buyTicketShouldFetchUserFromDatabaseCorrectlyWhenInputIsValid() {
        BuyTicketRequest buyTicketRequest = createBuyTicketRequest();
        User user = createTestUser();
        Event event = createTestEvent(user);
        String token = "token";
        String bankToken = "bankToken";
        String username = user.getUsername();

        Mockito.when(jwtService.extractUsername(token)).thenReturn(username);
        Mockito.when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        Mockito.when(eventRepository.findById(buyTicketRequest.getEventId())).thenReturn(Optional.of(event));

        mockWebServer.enqueue(new MockResponse()
                .setBody(bankToken)
                .setResponseCode(200));

        mockWebServer.enqueue(new MockResponse()
                .addHeader("Authorization", bankToken)
                .setResponseCode(200));

        paymentService.buyTicket(buyTicketRequest, token);

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(username);
    }

    @Test
    public void buyTicketShouldExtractUsernameFromTokenCorrectlyWhenTokenIsValid() {
        String expectedUsername = "username";
        String token = "token";
        Mockito.when(jwtService.extractUsername(token)).thenReturn(expectedUsername);

        String result = jwtService.extractUsername(token);

        Assertions.assertEquals(expectedUsername, result);
        Mockito.verify(jwtService, Mockito.times(1)).extractUsername(token);
    }

    @Test
    public void buyTicketSuccessfully() throws InterruptedException {
        BuyTicketRequest buyTicketRequest = createBuyTicketRequest();
        BankServiceLoginRequest bankServiceLoginRequest = createBankServiceLoginRequest(buyTicketRequest);
        User user = createTestUser();
        Event event = createTestEvent(user);
        Ticket ticket = createNewTicket(user, event);
        TransactionKafkaRequest transactionKafkaRequest = createTransactionKafkaRequest(buyTicketRequest, user, event);
        ExecuteTransactionRequest executeTransactionRequest = TransactionMapper.convertTransactionKafkaRequestToExecuteTransactionRequest(transactionKafkaRequest);
        executeTransactionRequest.setPassword(bankServiceLoginRequest.getPassword());
        String token = "token";
        String bankToken = "bankToken";
        String expectedCommunicate = "Ticket bought successfully";
        String username = user.getUsername();

        Mockito.when(jwtService.extractUsername(token)).thenReturn(username);
        Mockito.when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        Mockito.when(eventRepository.findById(buyTicketRequest.getEventId())).thenReturn(Optional.of(event));
        Mockito.when(ticketRepository.findByUser_IdAndEvent_Id(user.getId(), event.getId())).thenReturn(Optional.empty());

        mockWebServer.enqueue(new MockResponse()
                .setBody(bankToken)
                .setResponseCode(200));

        mockWebServer.enqueue(new MockResponse()
                .addHeader("Authorization", bankToken)
                .setResponseCode(200)
                .setBody(expectedCommunicate));

        String result = paymentService.buyTicket(buyTicketRequest, token);

        RecordedRequest loginRequestResult = mockWebServer.takeRequest();
        Assertions.assertEquals("POST", loginRequestResult.getMethod());
        Assertions.assertEquals("/clients/login", loginRequestResult.getPath());

        RecordedRequest executeTransactionRequestResult = mockWebServer.takeRequest();
        Assertions.assertEquals("PUT", executeTransactionRequestResult.getMethod());
        Assertions.assertEquals("/transactions/", executeTransactionRequestResult.getPath());

        Assertions.assertEquals(expectedCommunicate, result);

        Mockito.verify(jwtService, Mockito.times(1)).extractUsername(token);
        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(username);
        Mockito.verify(eventRepository, Mockito.times(1)).findById(buyTicketRequest.getEventId());
        Mockito.verify(ticketRepository, Mockito.times(1)).save(ticket);
    }

    private BuyTicketRequest createBuyTicketRequest() {
        return BuyTicketRequest.builder()
                .eventId(1L)
                .numberAccount("1234567890")
                .bankPassword("qwerty")
                .build();
    }

    private User createTestUser() {
        return User.builder()
                .id(1L)
                .firstName("Bil")
                .lastName("Smith")
                .email("bil@smith.com")
                .username("billy")
                .password("encodedPassword")
                .birthDate(LocalDate.of(1993, 4, 19))
                .role(Role.USER)
                .phoneNumber("123456789")
                .accountNumber("1234567890")
                .accountStatus("ACTIVE")
                .build();
    }

    private Event createTestEvent(User user) {
        return Event.builder()
                .id(1L)
                .eventName("Test Event")
                .eventDescription("Example description")
                .location("Lublin")
                .maxAttendees(10)
                .eventDate(LocalDate.of(2024, 6, 20))
                .eventStatus(EventStatus.ACTIVE)
                .ticketPrice(100.0)
                .eventTarget(EventTarget.EVERYBODY)
                .createdDate(LocalDateTime.of(2024, 1, 6, 10, 0))
                .modifiedDate(null)
                .participants(new ArrayList<>())
                .organizer(user)
                .build();
    }

    private User createTestUserOrganizer() {
        return User.builder()
                .id(2L)
                .firstName("Ed")
                .lastName("Walker")
                .email("eddy@walker.com")
                .username("eddy")
                .password("encodedPassword")
                .birthDate(LocalDate.of(1990, 1, 9))
                .role(Role.USER)
                .phoneNumber("987654321")
                .accountNumber("0987654321")
                .accountStatus("ACTIVE")
                .build();
    }

    private BankServiceLoginRequest createBankServiceLoginRequest(BuyTicketRequest buyTicketRequest) {
        return BankServiceLoginRequest.builder()
                .accountNumber(buyTicketRequest.getNumberAccount())
                .password(buyTicketRequest.getBankPassword())
                .build();
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

    private Ticket createNewTicket(User user, Event event) {
        return Ticket.builder()
                .hasTicket(true)
                .user(user)
                .event(event)
                .build();
    }
}
