package com.marcinsz.eventmanagementsystem.service;

import com.marcinsz.eventmanagementsystem.api.WeatherFromApi;
import com.marcinsz.eventmanagementsystem.configuration.WeatherApiConfig;
import com.marcinsz.eventmanagementsystem.dto.WeatherDto;
import com.marcinsz.eventmanagementsystem.exception.*;
import com.marcinsz.eventmanagementsystem.mapper.WeatherMapper;
import com.marcinsz.eventmanagementsystem.model.Event;
import com.marcinsz.eventmanagementsystem.model.EventStatus;
import com.marcinsz.eventmanagementsystem.repository.EventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;

@Slf4j
@Service
public class
WeatherService {
    private final WebClient webClient;
    private final EventRepository eventRepository;
    private final PolishCharactersMapper polishCharactersMapper;
    private final WeatherApiConfig weatherApiConfig;

    public WeatherService(@Qualifier("weatherWebClient") WebClient webClient,
                          EventRepository eventRepository,
                          PolishCharactersMapper polishCharactersMapper,
                          WeatherApiConfig weatherApiConfig) {

        this.webClient = webClient;
        this.eventRepository = eventRepository;
        this.polishCharactersMapper = polishCharactersMapper;
        this.weatherApiConfig = weatherApiConfig;
    }

    public WeatherDto weatherFromApi(Long eventId) {
        Event foundEvent = eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));
        validateEvent(foundEvent.getEventStatus());
        validateDate(foundEvent.getEventDate());
        return webClient.get()
                .uri(getUrlToCheckingWeatherForFoundEvent(foundEvent))
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        clientResponse -> clientResponse.bodyToMono(String.class)
                        .map((Function<String, Throwable>) responseBody -> {
                            if (responseBody.contains("1006")) {
                                log.error(responseBody);
                                throw new LocationNotFoundException("A location of this event is not handled.");
                            } else if (responseBody.contains("1003")) {
                                log.error(responseBody);
                                throw new EventValidateException("It looks like parameter q is missing.");
                            }
                            return new RuntimeException("Other error.");
                        }))
                .bodyToMono(WeatherFromApi.class)
                .map(WeatherMapper::mapToWeatherDto)
                .block();
    }

    private Function<UriBuilder, URI> getUrlToCheckingWeatherForFoundEvent(Event foundEvent) {
        return uriBuilder -> uriBuilder
                .queryParam("q", polishCharactersMapper.removePolishCharacters(foundEvent.getLocation()))
                .queryParam("dt", foundEvent.getEventDate())
                .queryParam("key", weatherApiConfig.getApiKey())
                .build();
    }

    private void validateDate(LocalDate eventDate) {
        long days = ChronoUnit.DAYS.between(LocalDate.now(), eventDate);
        if (days >= 14) {
            throw new EventForecastTooEarlyException("You can check the forecast only 14 days before the event.");
        }
        if (eventDate.isBefore(LocalDate.now())) {
            throw new EventValidateException("You can't check the forecast for a day before the event.");
        }
    }

    private void validateEvent(EventStatus eventStatus) {
        if (!eventStatus.equals(EventStatus.ACTIVE) && !eventStatus.equals(EventStatus.FULL)) {
            throw new EventValidateException("You can't check a forecast for a day of this event because the event has ended or cancelled.");
        }
    }
}
