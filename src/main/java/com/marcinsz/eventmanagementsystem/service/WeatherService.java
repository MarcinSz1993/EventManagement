package com.marcinsz.eventmanagementsystem.service;

import com.marcinsz.eventmanagementsystem.api.WeatherFromApi;
import com.marcinsz.eventmanagementsystem.request.WeatherRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private final WebClient webClient;

    public Mono<WeatherFromApi> weatherFromApi(WeatherRequest weatherRequest) {
        String apiKey = "7e546b0a93e0436896c160950242605";
        String baseUrl = "https://api.weatherapi.com/v1/forecast.json";
        return webClient.get()
                .uri(uriBuilder -> UriComponentsBuilder.fromUriString(baseUrl)
                        .queryParam("q", weatherRequest.getLocation())
                        .queryParam("dt", weatherRequest.getDate())
                        .queryParam("hour", weatherRequest.getHour())
                        .queryParam("key",apiKey)
                        .build().toUri())
                .header("accept", "application/json")
                .retrieve()
                .bodyToMono(WeatherFromApi.class);
    }
}
