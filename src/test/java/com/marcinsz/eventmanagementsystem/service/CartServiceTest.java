package com.marcinsz.eventmanagementsystem.service;

import com.marcinsz.eventmanagementsystem.exception.EventNotFoundException;
import com.marcinsz.eventmanagementsystem.model.*;
import com.marcinsz.eventmanagementsystem.repository.EventRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Optional;

class CartServiceTest {
    @InjectMocks
    private CartService cartService;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private HttpSession session;

    @Mock
    private HttpServletRequest httpServletRequest;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void addToCartShouldCorrectlyAddTicketToCartWhenCartIsNotEmpty() {
        Long eventId = 1L;
        User user = createTestUser();
        Event event1 = createTestEvent(user);
        Event event2 = createTestEvent(user);
        event2.setEventName("Test Event 2");

        Cart cart = new Cart(new LinkedHashMap<>());
        cart.addTicket(event1);
        Mockito.when(httpServletRequest.getSession()).thenReturn(session);
        Mockito.when(session.getAttribute("cart")).thenReturn(cart);
        Mockito.when(eventRepository.findById(eventId)).thenReturn(Optional.of(event2));

        cartService.addToCart(eventId, httpServletRequest);

        Assertions.assertNotNull(cart);
        Assertions.assertEquals(2, cart.getEvents().size());
        Assertions.assertTrue(cart.getEvents().containsKey("Test Event"));
        Assertions.assertTrue(cart.getEvents().containsKey("Test Event 2"));

    }

    @Test
    public void addToCartShouldCorrectlyAddTicketToCartWhenCartIsEmpty() {
        Long eventId = 1L;
        User user = createTestUser();
        Event event = createTestEvent(user);

        Mockito.when(httpServletRequest.getSession()).thenReturn(session);
        Mockito.when(session.getAttribute("cart")).thenReturn(new Cart(new LinkedHashMap<>()));
        Mockito.when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        cartService.addToCart(eventId, httpServletRequest);

        Cart cart = (Cart) session.getAttribute("cart");
        Assertions.assertNotNull(cart);
        Assertions.assertEquals(1, cart.getEvents().size());
        Assertions.assertTrue(cart.getEvents().containsKey(event.getEventName()));
    }

    @Test
    public void addToCartShouldThrowEventNotFoundExceptionWhenEventDoesNotExist(){
        Long notExistingEventId = 1L;
        Mockito.when(httpServletRequest.getSession()).thenReturn(session);
        Mockito.when(eventRepository.findById(notExistingEventId)).thenReturn(Optional.empty());

        EventNotFoundException eventNotFoundException = Assertions.assertThrows(EventNotFoundException.class, () -> cartService.addToCart(notExistingEventId, httpServletRequest));
        Assertions.assertEquals("The event with id: " + notExistingEventId + " does not exist.",eventNotFoundException.getMessage());

        Mockito.verify(httpServletRequest,Mockito.atLeastOnce()).getSession();
        Mockito.verify(eventRepository).findById(notExistingEventId);
    }

    @Test
    public void removeFromCartShouldCorrectlyRemoveTicketFromCartWhenCartIsNotEmpty() {
        User user = createTestUser();
        Event event1 = createTestEvent(user);
        Event event2 = createTestEvent(user);
        event2.setEventName("Event to delete");
        Cart cart = new Cart(new LinkedHashMap<>());
        cart.addTicket(event1);
        cart.addTicket(event2);
        String eventNameToDelete = "Event to delete";

        Mockito.when(httpServletRequest.getSession()).thenReturn(session);
        Mockito.when(session.getAttribute("cart")).thenReturn(cart);
        Mockito.when(eventRepository.findByEventName(eventNameToDelete)).thenReturn(Optional.of(event2));

        cartService.removeFromCart(eventNameToDelete,httpServletRequest);

        double expectedPrice = (event1.getTicketPrice() + event2.getTicketPrice()) - event2.getTicketPrice();

        Assertions.assertEquals(1, cart.getEvents().size());
        Assertions.assertEquals(expectedPrice,cart.getTotalPrice());
        Assertions.assertTrue(cart.getEvents().containsKey(event1.getEventName()));
        Assertions.assertFalse(cart.getEvents().containsKey(eventNameToDelete));

        Mockito.verify(httpServletRequest,Mockito.atLeast(1)).getSession();
        Mockito.verify(eventRepository).findByEventName(eventNameToDelete);
    }

    @Test
    public void removeFromCartShouldThrowEventNotFoundExceptionWhenEventDoesNotExistInCart(){
        String notExistingEventName = "Not existing eventName";
        Mockito.when(httpServletRequest.getSession()).thenReturn(session);
        Mockito.when(eventRepository.findByEventName(notExistingEventName)).thenReturn(Optional.empty());

        EventNotFoundException eventNotFoundException = Assertions.assertThrows(EventNotFoundException.class, () -> cartService.removeFromCart(notExistingEventName, httpServletRequest));
        Assertions.assertEquals("The event with event name: "  + notExistingEventName.toUpperCase() +  " does not exist.",eventNotFoundException.getMessage());

        Mockito.verify(httpServletRequest,Mockito.times(1)).getSession();
        Mockito.verify(eventRepository).findByEventName(notExistingEventName);
    }


    @Test
    public void showCartWhenAnyTicketsAreInCartShouldReturnActualStateOfCart(){
        User user = createTestUser();
        Event event1 = createTestEvent(user);
        Event event2 = createTestEvent(user);
        event2.setEventName("Test Event 2");
        Event event3 = createTestEvent(user);
        event3.setEventName("Test Event 3");

        Cart cart = new Cart(new LinkedHashMap<>());
        cart.addTicket(event1);
        cart.addTicket(event2);
        cart.addTicket(event3);

        Mockito.when(httpServletRequest.getSession()).thenReturn(session);
        Mockito.when(session.getAttribute("cart")).thenReturn(cart);

        Cart actualCart = cartService.showCart(httpServletRequest);

        Assertions.assertNotNull(actualCart);
        Assertions.assertEquals(3,actualCart.getEvents().size());
        Assertions.assertTrue(actualCart.getEvents().containsKey(event1.getEventName()));
        Assertions.assertTrue(actualCart.getEvents().containsKey(event2.getEventName()));
        Assertions.assertTrue(actualCart.getEvents().containsKey(event3.getEventName()));

        Mockito.verify(httpServletRequest,Mockito.times(1)).getSession();
    }

    @Test
    public void showCartShouldReturnEmptyHashMapWhenThereAreNoTicketsInCart(){
        Cart cart = new Cart(new LinkedHashMap<>());
        Mockito.when(httpServletRequest.getSession()).thenReturn(session);
        Mockito.when(session.getAttribute("cart")).thenReturn(cart);

        Cart actualCart = cartService.showCart(httpServletRequest);

        Assertions.assertNotNull(actualCart);
        Assertions.assertEquals(0,actualCart.getEvents().size());

        Mockito.verify(httpServletRequest).getSession();
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
}