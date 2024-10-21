package com.marcinsz.eventmanagementsystem.service;

import com.marcinsz.eventmanagementsystem.configuration.BankServiceConfig;
import com.marcinsz.eventmanagementsystem.exception.*;
import com.marcinsz.eventmanagementsystem.model.*;
import com.marcinsz.eventmanagementsystem.repository.EventRepository;
import com.marcinsz.eventmanagementsystem.repository.TicketRepository;
import com.marcinsz.eventmanagementsystem.repository.UserRepository;
import com.marcinsz.eventmanagementsystem.request.BuyTicketRequest;
import com.marcinsz.eventmanagementsystem.request.BuyTicketsFromCartRequest;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

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
    private KafkaMessageProducer kafkaMessageProducer;
    @Mock
    private HttpSession httpSession;
    @Mock
    private HttpServletRequest httpServletRequest;
    @InjectMocks
    private PaymentService paymentService;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("/").toString();
        Mockito.when(bankServiceConfig.getUrl()).thenReturn(baseUrl);
        Mockito.when(bankServiceConfig.getUserLogin()).thenReturn("/client/login");
        Mockito.when(bankServiceConfig.getTransaction()).thenReturn("/transactions/");


        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();

        paymentService = new PaymentService(webClient, jwtService, userRepository, eventRepository, ticketRepository, bankServiceConfig, kafkaMessageProducer);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void buyTicketSuccessfully() throws InterruptedException {
        String token = "token";
        User user = createTestUser();
        Event event = createTestEvent(user);
        String username = user.getUsername();
        BuyTicketRequest buyTicketRequest = createTestBuyTicketRequest();
        Ticket userTicket = createTestUserTicket(user, event);

        String expectedCommunicate = "Congratulations! You have successfully buy a ticket!";
        String tokenFromBankService = "BankServiceToken";


        mockingBasicDependenciesForTestingPaymentServiceClass(token, username, user, event.getId(), event, Optional.ofNullable(userTicket));

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(tokenFromBankService));

        mockWebServer.enqueue(new MockResponse()
                        .setResponseCode(200)
                        .addHeader("Authorization", tokenFromBankService)
                        .addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                        .addHeader((HttpHeaders.CACHE_CONTROL),"no-cache")
                        .addHeader("Content-Type", "application/json")
                        .setBody(expectedCommunicate));


        paymentService.buyTicket(buyTicketRequest, token);

        Assertions.assertNotNull(userTicket);
        Mockito.verify(ticketRepository).save(userTicket);
        assertTrue(userTicket.isHasTicket());

        RecordedRequest loginRequest = mockWebServer.takeRequest();
        Assertions.assertEquals("POST", loginRequest.getMethod());
        Assertions.assertEquals("/client/login", loginRequest.getPath());


        RecordedRequest transactionRequest = mockWebServer.takeRequest();
        Assertions.assertEquals("PUT", transactionRequest.getMethod());
        Assertions.assertEquals("/transactions/", transactionRequest.getPath());
        assertTrue(Objects.requireNonNull(transactionRequest.getHeader(HttpHeaders.AUTHORIZATION)).contains("BankServiceToken"));
        assertTrue(Objects.requireNonNull(transactionRequest.getHeader(HttpHeaders.ACCEPT)).contains("application/json"));
        assertTrue(Objects.requireNonNull(transactionRequest.getHeader(HttpHeaders.CACHE_CONTROL)).contains("no-cache"));
        assertTrue(Objects.requireNonNull(transactionRequest.getHeader(HttpHeaders.CONTENT_TYPE)).contains("application/json"));

    }
    @Test
    public void buyTicketShouldThrowTransactionProcessClientExceptionWhenClientErrorOccurs() throws InterruptedException {
        User user = createTestUser();
        Event event = createTestEvent(user);
        BuyTicketRequest buyTicketRequest = createTestBuyTicketRequest();
        Ticket userTicket = createTestUserTicket(user, event);
        String token = "token";
        String username = user.getUsername();
        String tokenFromBankService = "BankServiceToken";

        mockingBasicDependenciesForTestingPaymentServiceClass(token, username, user, event.getId(), event, Optional.ofNullable(userTicket));

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(tokenFromBankService));

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setBody("Client error while processing transaction."));

        TransactionProcessClientException transactionProcessClientException = assertThrows(TransactionProcessClientException.class, () -> paymentService.buyTicket(buyTicketRequest, token));
        assertEquals("Client error while processing transaction.",transactionProcessClientException.getMessage());
        assertNotNull(userTicket);

        RecordedRequest loginRequest = mockWebServer.takeRequest();
        assertEquals("POST", loginRequest.getMethod());
        assertEquals("/client/login",loginRequest.getPath());

        RecordedRequest transactionRequest = mockWebServer.takeRequest();
        assertEquals("PUT",transactionRequest.getMethod());
        assertEquals("/transactions/",transactionRequest.getPath());
        assertTrue(Objects.requireNonNull(transactionRequest.getHeader(HttpHeaders.AUTHORIZATION)).contains("BankServiceToken"));

        Mockito.verify(jwtService).extractUsername(token);
        Mockito.verify(userRepository).findByUsername(username);
        Mockito.verify(eventRepository).findById(buyTicketRequest.getEventId());
        Mockito.verify(kafkaMessageProducer,Mockito.never()).sendTransactionRequestMessageToExpectingPaymentsTopic(Mockito.any(TransactionKafkaRequest.class));
        Mockito.verify(ticketRepository,Mockito.never()).save(userTicket);
    }

    @Test
    public void buyTicketShouldThrowTransactionProcessServerExceptionWhenServerErrorOccurs() throws InterruptedException {

        User user = createTestUser();
        Event event = createTestEvent(user);
        BuyTicketRequest buyTicketRequest = createTestBuyTicketRequest();
        Ticket userTicket = createTestUserTicket(user, event);
        String token = "token";
        String username = user.getUsername();
        String tokenFromBankService = "BankServiceToken";

        mockingBasicDependenciesForTestingPaymentServiceClass(token, username, user, event.getId(), event, Optional.ofNullable(userTicket));

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(tokenFromBankService));

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Error error while processing transaction."));

        TransactionProcessServerException transactionProcessClientException = assertThrows(TransactionProcessServerException.class, () -> paymentService.buyTicket(buyTicketRequest, token));
        assertEquals("Server error while processing transaction.",transactionProcessClientException.getMessage());
        assertNotNull(userTicket);

        RecordedRequest loginRequest = mockWebServer.takeRequest();
        assertEquals("POST", loginRequest.getMethod());
        assertEquals("/client/login",loginRequest.getPath());

        RecordedRequest transactionRequest = mockWebServer.takeRequest();
        assertEquals("PUT",transactionRequest.getMethod());
        assertEquals("/transactions/",transactionRequest.getPath());
        assertTrue(Objects.requireNonNull(transactionRequest.getHeader(HttpHeaders.AUTHORIZATION)).contains("BankServiceToken"));

        Mockito.verify(jwtService).extractUsername(token);
        Mockito.verify(userRepository).findByUsername(username);
        Mockito.verify(eventRepository).findById(buyTicketRequest.getEventId());
        Mockito.verify(kafkaMessageProducer,Mockito.never()).sendTransactionRequestMessageToExpectingPaymentsTopic(Mockito.any(TransactionKafkaRequest.class));
        Mockito.verify(ticketRepository,Mockito.never()).save(userTicket);
    }

    @Test
    public void buyTicketShouldThrowTicketAlreadyBoughtExceptionWhenUserAlreadyHasTicketOnTheEvent(){
        User user = createTestUser();
        Event event = createTestEvent(user);
        String username = user.getUsername();
        String token = "token";
        BuyTicketRequest buyTicketRequest = createTestBuyTicketRequest();
        Ticket userTicket = createTestUserTicket(user, event);
        userTicket.setHasTicket(true);

        mockingBasicDependenciesForTestingPaymentServiceClass(token, username, user, buyTicketRequest.getEventId(), event, Optional.of(userTicket));

        TicketAlreadyBoughtException ticketAlreadyBoughtException = assertThrows(TicketAlreadyBoughtException.class, () -> paymentService.buyTicket(buyTicketRequest, token));
        assertEquals("You have a ticket for this event.",ticketAlreadyBoughtException.getMessage());

        Mockito.verify(jwtService).extractUsername(token);
        Mockito.verify(userRepository).findByUsername(username);
        Mockito.verify(eventRepository).findById(buyTicketRequest.getEventId());
        Mockito.verify(kafkaMessageProducer,Mockito.never()).sendTransactionRequestMessageToExpectingPaymentsTopic(new TransactionKafkaRequest());
        Mockito.verify(ticketRepository,Mockito.never()).save(userTicket);
    }

    @Test
    public void buyTicketShouldSendMessageToKafkaWhenBankServiceServerNotAvailableWasThrown() throws IOException {
        //Ten test mi wykazał, że być może niepotrzebnie w dwóch miejscach w kodzie w klasie serwisowej
        //wysyłam wiadomość do Kafki. Obecnie jest to robione podczas nieudanego zalogowania się do banku
        //w pierwszym żądaniu, jak i po przechywceniu wyjątku w drugim żądaniu, ale chyba przez
        //błąd rzucony w pierwszym żądaniu do drugiego nigdy nie dochodzi. Mówię o sytuacji, gdy aplikacja bankowa
        //jest wyłączona/offline. <--- DO ZWERYFIKOWANIA!
        User user = createTestUser();
        Event event = createTestEvent(user);
        String username = user.getUsername();
        String token = "token";
        BuyTicketRequest buyTicketRequest = createTestBuyTicketRequest();
        Ticket userTicket = createTestUserTicket(user, event);

        mockingBasicDependenciesForTestingPaymentServiceClass(token, username, user, buyTicketRequest.getEventId(), event, Optional.of(userTicket));
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(503)
                .setBody("Bank Service server is not available"));
        mockWebServer.shutdown();

        assertThrows(BankServiceServerNotAvailableException.class,() -> paymentService.buyTicket(buyTicketRequest, token));

        ArgumentCaptor<TransactionKafkaRequest> kafkaRequestCaptor = ArgumentCaptor.forClass(TransactionKafkaRequest.class);
        Mockito.verify(kafkaMessageProducer).sendTransactionRequestMessageToExpectingPaymentsTopic(kafkaRequestCaptor.capture());

        TransactionKafkaRequest capturedKafkaRequest = kafkaRequestCaptor.getValue();
        assertEquals(TransactionType.ONLINE_PAYMENT,capturedKafkaRequest.getTransactionType());
        assertEquals(event.getId(), capturedKafkaRequest.getEventId());
        assertEquals(user.getId(),capturedKafkaRequest.getUserId());
        assertEquals(event.getOrganizer().getAccountNumber(),capturedKafkaRequest.getAccountNumber());
        assertEquals(user.getAccountNumber(),capturedKafkaRequest.getAccountNumber());
        assertEquals(event.getTicketPrice(), capturedKafkaRequest.getAmount());
    }
    @Test
    public void buyTicketShouldThrowBankServiceServerNotAvailableExceptionWhenBankServiceIsOff() throws InterruptedException {
        User user = createTestUser();
        Event event = createTestEvent(user);
        String username = user.getUsername();
        String token = "token";
        BuyTicketRequest buyTicketRequest = createTestBuyTicketRequest();
        Ticket userTicket = createTestUserTicket(user, event);

        mockingBasicDependenciesForTestingPaymentServiceClass(token, username, user, buyTicketRequest.getEventId(), event, Optional.of(userTicket));

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(503)
                .setBody("Bank Service server is not available"));

        BankServiceServerNotAvailableException bankServiceServerNotAvailableException = assertThrows(BankServiceServerNotAvailableException.class, () -> paymentService.buyTicket(buyTicketRequest, token));

        RecordedRequest loginRequest = mockWebServer.takeRequest();

        assertEquals("POST", loginRequest.getMethod());
        assertEquals("/client/login",loginRequest.getPath());
        assertEquals("Bank Service server is not available",bankServiceServerNotAvailableException.getMessage());

        Mockito.verify(jwtService).extractUsername(token);
        Mockito.verify(userRepository).findByUsername(username);
        Mockito.verify(eventRepository).findById(buyTicketRequest.getEventId());
        Mockito.verify(ticketRepository,Mockito.never()).save(userTicket);
    }

    @Test
    public void buyTicketShouldThrowBankServiceServerNotAvailableException() throws IOException {
        User user = createTestUser();
        Event event = createTestEvent(user);
        String username = user.getUsername();
        String token = "token";
        BuyTicketRequest buyTicketRequest = createTestBuyTicketRequest();
        Ticket userTicket = createTestUserTicket(user, event);

        mockingBasicDependenciesForTestingPaymentServiceClass(token, username, user, buyTicketRequest.getEventId(), event, Optional.of(userTicket));

        mockWebServer.shutdown();

        assertThrows(BankServiceServerNotAvailableException.class, () -> paymentService.buyTicket(buyTicketRequest, token));

    }

    @Test
    public void buyTicketsFromCartSuccessfully(){
        //Ten test wykazał, że należy chyba zmienić implementacje logiki biznesowej w serwisie.
        //Chodzi o to, że zauważyłem iż logowanie do banku wykonuje się w pętli, a uważam,
        //że jeden raz wystarczy. <---- DO ZWERYFIKOWANIA.
        User user = createTestUser();
        Event event1 = createTestEvent(user);
        Event event2 = createTestEvent(user);
        event2.setEventName("Test event 2");
        event2.setId(2L);
        String username = user.getUsername();
        BuyTicketsFromCartRequest buyTicketsFromCartRequest = createTestBuyTicketsFromCartRequest();
        String token = "token";
        String bankServiceToken = "bankServiceToken";
        Ticket userTicket1 = createTestUserTicket(user, event1);
        Ticket userTicket2 = createTestUserTicket(user, event2);
        userTicket2.setId(2L);
        Cart cart = new Cart(new LinkedHashMap<>());
        cart.addTicket(event1);
        cart.addTicket(event2);

        Mockito.when(httpSession.getAttribute("cart")).thenReturn(cart);
        Mockito.when(httpServletRequest.getSession()).thenReturn(httpSession);
        Mockito.when(jwtService.extractUsername(token)).thenReturn(username);
        Mockito.when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        Mockito.when(eventRepository.findByEventName(event1.getEventName())).thenReturn(Optional.of(event1));
        Mockito.when(eventRepository.findByEventName(event2.getEventName())).thenReturn(Optional.of(event2));

        Mockito.when(ticketRepository.findByUser_IdAndEvent_Id(user.getId(), event1.getId())).thenReturn(Optional.of(userTicket1));
        Mockito.when(ticketRepository.findByUser_IdAndEvent_Id(user.getId(), event2.getId())).thenReturn(Optional.of(userTicket2));


        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(bankServiceToken));

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("Tickets have been purchased."));

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(bankServiceToken));

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("Tickets have been purchased."));

        paymentService.buyTicketsFromCart(buyTicketsFromCartRequest,token,httpServletRequest);

        Mockito.verify(ticketRepository,Mockito.times(2)).save(Mockito.any(Ticket.class));
        assertTrue(userTicket1.isHasTicket());
        assertTrue(userTicket2.isHasTicket());
    }

    @Test
    public void buyTicketsFromCartShouldThrowEmptyCartExceptionWhenCartIsEmpty(){
        BuyTicketsFromCartRequest buyTicketsFromCartRequest = createTestBuyTicketsFromCartRequest();
        String token = "token";
        Cart emptyCart = new Cart(new LinkedHashMap<>());

        Mockito.when(httpSession.getAttribute("cart")).thenReturn(emptyCart);
        Mockito.when(httpServletRequest.getSession()).thenReturn(httpSession);

        EmptyCartException emptyCartException = assertThrows(EmptyCartException.class, () -> paymentService.buyTicketsFromCart(buyTicketsFromCartRequest, token, httpServletRequest));
        assertEquals("Your cart is empty.", emptyCartException.getMessage());

        Mockito.verify(httpSession,Mockito.times(1)).getAttribute("cart");
        Mockito.verify(httpServletRequest,Mockito.times(1)).getSession();
    }

    @Test
    public void buyTicketsFromCartShouldSendMessageToKafkaWhenBankServiceIsNotAvailable() throws IOException {
        User user = createTestUser();
        User eventOrganizer = createTestUserOrganizer();
        Event event1 = createTestEvent(eventOrganizer);
        Event event2 = createTestEvent(eventOrganizer);
        event2.setEventName("Test event 2");
        event2.setId(2L);
        String username = user.getUsername();
        BuyTicketsFromCartRequest testBuyTicketsFromCartRequest = createTestBuyTicketsFromCartRequest();
        String token = "token";
        Cart cart = new Cart(new LinkedHashMap<>());
        cart.addTicket(event1);
        cart.addTicket(event2);
        Ticket userTicket1 = createTestUserTicket(user, event1);
        Ticket userTicket2 = createTestUserTicket(user, event2);
        userTicket2.setId(2L);

        Mockito.when(httpSession.getAttribute("cart")).thenReturn(cart);
        Mockito.when(httpServletRequest.getSession()).thenReturn(httpSession);

        Mockito.when(jwtService.extractUsername(token)).thenReturn(username);
        Mockito.when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        Mockito.when(eventRepository.findByEventName(event1.getEventName())).thenReturn(Optional.of(event1));
        Mockito.when(eventRepository.findByEventName(event2.getEventName())).thenReturn(Optional.of(event2));

        Mockito.when(ticketRepository.findByUser_IdAndEvent_Id(user.getId(), event1.getId())).thenReturn(Optional.of(userTicket1));
        Mockito.when(ticketRepository.findByUser_IdAndEvent_Id(user.getId(), event2.getId())).thenReturn(Optional.of(userTicket2));

        mockWebServer.shutdown();

        Mockito.doNothing().when(kafkaMessageProducer).sendTransactionRequestMessageToExpectingPaymentsTopic(Mockito.any(TransactionKafkaRequest.class));
        assertThrows(BankServiceServerNotAvailableException.class, () -> paymentService.buyTicketsFromCart(testBuyTicketsFromCartRequest, token, httpServletRequest));

        ArgumentCaptor<TransactionKafkaRequest> transactionKafkaRequestArgumentCaptor = ArgumentCaptor.forClass(TransactionKafkaRequest.class);
        Mockito.verify(kafkaMessageProducer, Mockito.times(2)).sendTransactionRequestMessageToExpectingPaymentsTopic(transactionKafkaRequestArgumentCaptor.capture());

        List<TransactionKafkaRequest> transactionKafkaRequestList = transactionKafkaRequestArgumentCaptor.getAllValues();

        assertNotNull(transactionKafkaRequestList.getFirst());
        assertEquals(user.getAccountNumber(), transactionKafkaRequestList.getFirst().getAccountNumber());
        assertEquals(event1.getTicketPrice(), transactionKafkaRequestList.getFirst().getAmount());
        assertEquals(TransactionType.ONLINE_PAYMENT, transactionKafkaRequestList.getFirst().getTransactionType());
        assertEquals(user.getId(), transactionKafkaRequestList.getFirst().getUserId());
        assertEquals(event1.getId(), transactionKafkaRequestList.get(0).getEventId());
        assertEquals(event1.getOrganizer().getAccountNumber(), transactionKafkaRequestList.get(0).getOrganizerBankAccountNumber());

        assertNotNull(transactionKafkaRequestList.get(1));
        assertEquals(user.getAccountNumber(), transactionKafkaRequestList.get(1).getAccountNumber());
        assertEquals(event2.getTicketPrice(), transactionKafkaRequestList.get(1).getAmount());
        assertEquals(TransactionType.ONLINE_PAYMENT, transactionKafkaRequestList.get(1).getTransactionType());
        assertEquals(user.getId(), transactionKafkaRequestList.get(1).getUserId());
        assertEquals(event2.getId(), transactionKafkaRequestList.get(1).getEventId());
        assertEquals(event2.getOrganizer().getAccountNumber(), transactionKafkaRequestList.get(1).getOrganizerBankAccountNumber());

        Mockito.verify(kafkaMessageProducer, Mockito.times(2)).sendTransactionRequestMessageToExpectingPaymentsTopic(Mockito.any(TransactionKafkaRequest.class));
    }

    @Test
    public void buyTicketsShouldThrowBankServiceNotAvailableExceptionWhenBankServiceIsNotAvailable() throws IOException {
        User user = createTestUser();
        User eventOrganizer = createTestUserOrganizer();
        Event event1 = createTestEvent(eventOrganizer);
        Event event2 = createTestEvent(eventOrganizer);
        event2.setEventName("Test event 2");
        event2.setId(2L);
        String username = user.getUsername();
        BuyTicketsFromCartRequest buyTicketsFromCartRequest = createTestBuyTicketsFromCartRequest();
        String token = "token";
        Cart cart = new Cart(new LinkedHashMap<>());
        cart.addTicket(event1);
        cart.addTicket(event2);
        Ticket userTicket1 = createTestUserTicket(user, event1);
        Ticket userTicket2 = createTestUserTicket(user, event2);
        userTicket2.setId(2L);

        Mockito.when(httpSession.getAttribute("cart")).thenReturn(cart);
        Mockito.when(httpServletRequest.getSession()).thenReturn(httpSession);

        Mockito.when(jwtService.extractUsername(token)).thenReturn(username);
        Mockito.when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        Mockito.when(eventRepository.findByEventName(event1.getEventName())).thenReturn(Optional.of(event1));
        Mockito.when(eventRepository.findByEventName(event2.getEventName())).thenReturn(Optional.of(event2));

        Mockito.when(ticketRepository.findByUser_IdAndEvent_Id(user.getId(), event1.getId())).thenReturn(Optional.of(userTicket1));
        Mockito.when(ticketRepository.findByUser_IdAndEvent_Id(user.getId(), event2.getId())).thenReturn(Optional.of(userTicket2));

        mockWebServer.shutdown();

        BankServiceServerNotAvailableException bankServiceServerNotAvailableException = assertThrows(BankServiceServerNotAvailableException.class, () -> paymentService.buyTicketsFromCart(buyTicketsFromCartRequest, token, httpServletRequest));
        assertEquals("Bank Service server is not available",bankServiceServerNotAvailableException.getMessage());

    }

    private BuyTicketsFromCartRequest createTestBuyTicketsFromCartRequest() {
        return BuyTicketsFromCartRequest.builder()
                .numberAccount("1234567890")
                .bankPassword("qwerty")
                .build();
    }

    private void mockingBasicDependenciesForTestingPaymentServiceClass(String token, String username, User user, Long buyTicketRequest, Event event, Optional<Ticket> userTicket) {
        Mockito.when(jwtService.extractUsername(token)).thenReturn(username);
        Mockito.when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        Mockito.when(eventRepository.findById(buyTicketRequest)).thenReturn(Optional.of(event));
        Mockito.when(ticketRepository.findByUser_IdAndEvent_Id(user.getId(), event.getId())).thenReturn(userTicket);
    }

    private Ticket createTestUserTicket(User user, Event event) {
        return Ticket.builder()
                .id(1L)
                .hasTicket(false)
                .user(user)
                .event(event)
                .build();
    }

    private BuyTicketRequest createTestBuyTicketRequest() {
        return BuyTicketRequest.builder()
                .eventId(1L)
                .numberAccount("1234567890")
                .bankPassword("qwerty")
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

    private User createTestUser() {
        return User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Smith")
                .email("john@smith.com")
                .username("johnny")
                .password("encodedPassword")
                .birthDate(LocalDate.of(1993, 4, 19))
                .role(Role.USER)
                .phoneNumber("123456789")
                .accountNumber("1234567890")
                .accountStatus("ACTIVE")
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
}
