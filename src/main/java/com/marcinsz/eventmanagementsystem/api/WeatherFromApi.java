package com.marcinsz.eventmanagementsystem.api;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WeatherFromApi {
    private Location location;
    private Current current;
    private Forecast forecast;
}
