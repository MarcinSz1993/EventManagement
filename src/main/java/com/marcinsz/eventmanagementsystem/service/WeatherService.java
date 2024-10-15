package com.marcinsz.eventmanagementsystem.service;

import com.marcinsz.eventmanagementsystem.api.WeatherFromApi;
import com.marcinsz.eventmanagementsystem.configuration.WeatherApiConfig;
import com.marcinsz.eventmanagementsystem.dto.WeatherDto;
import com.marcinsz.eventmanagementsystem.exception.EventForecastTooEarlyException;
import com.marcinsz.eventmanagementsystem.exception.EventNotFoundException;
import com.marcinsz.eventmanagementsystem.mapper.WeatherMapper;
import com.marcinsz.eventmanagementsystem.model.Event;
import com.marcinsz.eventmanagementsystem.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class
WeatherService {

    private final WebClient webClient;
    private final EventRepository eventRepository;
    private final PolishCharactersMapper polishCharactersMapper;
    private final WeatherApiConfig weatherApiConfig;

    public WeatherDto weatherFromApi(Long eventId) {
        Event foundEvent = eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));
        if(validateDate(foundEvent.getEventDate())){
           throw new EventForecastTooEarlyException("You can check the forecast only 14 days before the event.");
        }
        String baseUrl = weatherApiConfig.getBaseUrl();
        String fullUrl = UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("q", polishCharactersMapper.removePolishCharacters(foundEvent.getLocation()))
                .queryParam("dt", foundEvent.getEventDate())
                .queryParam("key", weatherApiConfig.getApiKey())
                .toUriString();
        return webClient.get()
                    .uri(fullUrl)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .bodyToMono(WeatherFromApi.class)
                    .map(WeatherMapper::convertWeatherFromApiToWeatherDto)
                    .block();
    }

    private boolean validateDate(LocalDate eventDate){
        long days = ChronoUnit.DAYS.between(LocalDate.now(), eventDate);
        return days >= 14;
    }
}
