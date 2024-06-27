package com.marcinsz.eventmanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherDto {
    private String date;
    private String cityName;
    private String country;
    private String sunrise;
    private String sunset;
    private int maxTemperature;
    private int minTemperature;
    private int maxWind;
    private int chanceOfRain;
    private int chanceOfSnow;
}
