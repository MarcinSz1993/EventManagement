package com.marcinsz.eventmanagementsystem.service;

import com.marcinsz.eventmanagementsystem.api.WeatherFromApi;
import com.marcinsz.eventmanagementsystem.dto.WeatherDto;
import com.marcinsz.eventmanagementsystem.exception.EventNotFoundException;
import com.marcinsz.eventmanagementsystem.mapper.WeatherMapper;
import com.marcinsz.eventmanagementsystem.model.Event;
import com.marcinsz.eventmanagementsystem.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private final WebClient webClient;
    private final EventRepository eventRepository;
    private final PolishCharactersRemover polishCharactersRemover;

    public WeatherDto weatherFromApi(Long eventId) throws Throwable {
        Event foundEvent = eventRepository.findById(eventId)
                .orElseThrow((Supplier<Throwable>) () ->
                        new EventNotFoundException(eventId));
        String apiKey = "7e546b0a93e0436896c160950242605";
        String baseUrl = "https://api.weatherapi.com/v1/forecast.json";
        return webClient.get()
                .uri(uriBuilder -> UriComponentsBuilder.fromUriString(baseUrl)
                        .queryParam("q", polishCharactersRemover.removePolishCharacters(foundEvent.getLocation()))
                        .queryParam("dt", foundEvent.getEventDate())
                        .queryParam("key", apiKey)
                        .build().toUri())
                .header("accept", "application/json")
                .retrieve()
                .bodyToMono(WeatherFromApi.class)
                .map(WeatherMapper::convertWeatherFromApiToWeatherDto)
                .block();
    }
}
