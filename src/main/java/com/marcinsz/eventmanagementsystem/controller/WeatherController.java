package com.marcinsz.eventmanagementsystem.controller;

import com.marcinsz.eventmanagementsystem.api.WeatherFromApi;
import com.marcinsz.eventmanagementsystem.request.WeatherRequest;
import com.marcinsz.eventmanagementsystem.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@RestController
@RequestMapping("/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;
    @GetMapping("/")
    public Mono<WeatherFromApi> getWeather(@RequestParam String location,
                                           @RequestParam LocalDate date,
                                           @RequestParam int hour){
        WeatherRequest weatherRequest = new WeatherRequest(location,date,hour);
        return weatherService.weatherFromApi(weatherRequest);
    }
}
