package com.marcinsz.eventmanagementsystem.api;

import lombok.Data;

import java.util.List;

@Data
public class Forecast {
    private List<Forecastday> forecastDay;
}
