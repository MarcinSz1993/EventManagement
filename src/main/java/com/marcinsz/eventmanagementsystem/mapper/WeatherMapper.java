package com.marcinsz.eventmanagementsystem.mapper;

import com.marcinsz.eventmanagementsystem.api.*;
import com.marcinsz.eventmanagementsystem.dto.WeatherDto;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WeatherMapper {
    public static WeatherDto convertWeatherFromApiToWeatherDto(WeatherFromApi weatherFromApi) {
        Day day = weatherFromApi.getForecast().getForecastDay().getFirst().getDay();
        Astro astro = weatherFromApi.getForecast().getForecastDay().getFirst().getAstro();
        Location location = weatherFromApi.getLocation();
        Forecastday forecastday = weatherFromApi.getForecast().getForecastDay().getFirst();
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
