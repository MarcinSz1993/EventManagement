package com.marcinsz.eventmanagementsystem.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Day {
    @JsonProperty("maxtemp_c")
    private int maxtempC;
    @JsonProperty("maxtemp_f")
    private int maxtempF;
    @JsonProperty("mintemp_c")
    private int mintempC;
    @JsonProperty("mintemp_f")
    private int mintempF;
    @JsonProperty("avgtemp_c")
    private int avgtempC;
    @JsonProperty("avgtemp_f")
    private int avgtempF;
    @JsonProperty("maxwind_mph")
    private int maxwindMph;
    @JsonProperty("maxwind_kph")
    private int maxwindKph;
    @JsonProperty("totalprecip_mm")
    private int totalprecipMm;
    @JsonProperty("totalprecip_in")
    private int totalprecipIn;
    @JsonProperty("totalsnow_cm")
    private int totalsnowCm;
    @JsonProperty("avgvis_km")
    private int avgvisKm;
    @JsonProperty("avgvis_miles")
    private int avgvisMiles;
    private int avghumidity;
    @JsonProperty("daily_will_it_rain")
    private int dailyWillItRain;
    @JsonProperty("daily_chance_of_rain")
    private int dailyChanceOfRain;
    @JsonProperty("daily_will_it_snow")
    private int dailyWillItSnow;
    @JsonProperty("daily_chance_of_snow")
    private int dailyChanceOfSnow;
    private Condition condition;
    private int uv;
}
