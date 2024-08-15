package com.marcinsz.eventmanagementsystem.mapper;

import com.marcinsz.eventmanagementsystem.api.*;
import com.marcinsz.eventmanagementsystem.dto.WeatherDto;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WeatherMapper {
    public static WeatherDto convertWeatherFromApiToWeatherDto(WeatherFromApi weatherFromApi) {
        Day day = weatherFromApi.getForecast().getForecastday().getFirst().getDay();
        Astro astro = weatherFromApi.getForecast().getForecastday().getFirst().getAstro();
        Location location = weatherFromApi.getLocation();
        Forecastday forecastday = weatherFromApi.getForecast().getForecastday().getFirst();
        return new WeatherDto(forecastday.getDate(),
                location.getName(),
                location.getCountry(),
                astro.getSunrise(),
                astro.getSunset(),
                day.getMaxtempC(),
                day.getMintempC(),
                day.getMaxwindKph(),
                day.getDailyChanceOfRain(),
                day.getDailyChanceOfSnow());
    }
}
