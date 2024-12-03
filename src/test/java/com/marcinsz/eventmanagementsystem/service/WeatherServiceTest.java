package com.marcinsz.eventmanagementsystem.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcinsz.eventmanagementsystem.api.WeatherFromApi;
import com.marcinsz.eventmanagementsystem.configuration.WeatherApiConfig;
import com.marcinsz.eventmanagementsystem.dto.WeatherDto;
import com.marcinsz.eventmanagementsystem.exception.EventForecastTooEarlyException;
import com.marcinsz.eventmanagementsystem.exception.EventNotFoundException;
import com.marcinsz.eventmanagementsystem.model.*;
import com.marcinsz.eventmanagementsystem.repository.EventRepository;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class WeatherServiceTest {
    private MockWebServer mockWebServer;
    private ObjectMapper objectMapper;
    @Mock
    EventRepository eventRepository;

    @Mock
    PolishCharactersMapper polishCharactersMapper;

    @Mock
    WeatherApiConfig weatherApiConfig;

    @InjectMocks
    WeatherService weatherService;



    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        objectMapper = new ObjectMapper();

        String baseUrl = mockWebServer.url("/").toString();
        when(weatherApiConfig.getBaseUrl()).thenReturn(baseUrl);

        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();

        weatherService = new WeatherService(webClient, eventRepository, polishCharactersMapper, weatherApiConfig);
    }

    @Test
    public void weatherFromApiShouldReturnForecastCorrectly() throws IOException {
        User user = createTestUser();
        Event event = createTestEvent(user);
        event.setEventDate(LocalDate.now().plusDays(1));
        Long eventId = event.getId();
        String locationWithoutPolishCharacters = "locationWithoutPolishCharacters";

        WeatherFromApi weatherFromApi = objectMapper.readValue(new File("src/test/weatherApiTestData.json"), WeatherFromApi.class);
        String expectedJsonResponse = objectMapper.writeValueAsString(weatherFromApi);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(polishCharactersMapper.removePolishCharacters(event.getLocation())).thenReturn(locationWithoutPolishCharacters);

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(expectedJsonResponse)
                .addHeader("Content-Type", "application/json"));

        WeatherDto actualWeatherDto = weatherService.weatherFromApi(eventId);

        assertEquals(18, actualWeatherDto.getMaxTemperature());
        assertEquals("2024-10-19",actualWeatherDto.getDate());
        assertEquals("London",actualWeatherDto.getCityName());
        assertEquals("UK",actualWeatherDto.getCountry());
        assertEquals("07:45 AM",actualWeatherDto.getSunrise());
        assertEquals("06:00 PM",actualWeatherDto.getSunset());
    }

    @Test
    public void weatherFromApiShouldThrowEventForecastTooEarlyExceptionWhenEventIsMoreThan14DaysInTheFuture(){
        User user = createTestUser();
        Event event = createTestEvent(user);
        Long eventId = event.getId();
        event.setEventDate(LocalDate.of(2025,12,12));

        Mockito.when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        EventForecastTooEarlyException eventForecastTooEarlyException = assertThrows(EventForecastTooEarlyException.class, () -> weatherService.weatherFromApi(eventId));
        assertEquals("You can check the forecast only 14 days before the event.",eventForecastTooEarlyException.getMessage());

        Mockito.verify(eventRepository).findById(eventId);
    }

    @Test
    public void weatherFromApiShouldThrowEventNotFoundExceptionWhenEventIsNotFound(){
        Long notExistingEventId = 100L;

        Mockito.when(eventRepository.findById(notExistingEventId)).thenReturn(Optional.empty());

        EventNotFoundException eventNotFoundException = assertThrows(EventNotFoundException.class, () -> weatherService.weatherFromApi(notExistingEventId));
        assertEquals("Event with id " + notExistingEventId + " not found",eventNotFoundException.getMessage());

        Mockito.verify(eventRepository).findById(notExistingEventId);
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