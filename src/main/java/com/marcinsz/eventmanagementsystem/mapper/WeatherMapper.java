package com.marcinsz.eventmanagementsystem.mapper;

import com.marcinsz.eventmanagementsystem.api.WeatherFromApi;
import com.marcinsz.eventmanagementsystem.dto.WeatherDto;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WeatherMapper {

    public static WeatherDto convertWeatherFromApiToWeatherDto(WeatherFromApi weatherFromApi){
        return new WeatherDto(weatherFromApi.getForecast().getForecastday().get(0).getDate(),
                              weatherFromApi.getLocation().getName(),
                              weatherFromApi.getLocation().getCountry(),
                              weatherFromApi.getForecast().getForecastday().get(0).getAstro().getSunrise(),
                              weatherFromApi.getForecast().getForecastday().get(0).getAstro().getSunset(),
                              weatherFromApi.getForecast().getForecastday().get(0).getDay().getMaxtempC(),
                              weatherFromApi.getForecast().getForecastday().get(0).getDay().getMintempC(),
                              weatherFromApi.getForecast().getForecastday().get(0).getDay().getMaxwindKph(),
                              weatherFromApi.getForecast().getForecastday().get(0).getDay().getDailyChanceOfRain(),
                              weatherFromApi.getForecast().getForecastday().get(0).getDay().getDailyChanceOfSnow());

    }
}
