package com.marcinsz.eventmanagementsystem.api;

import lombok.Data;

@Data
public class WeatherFromApi {
    private Location location;
    private Current current;
    private Forecast forecast;
}
