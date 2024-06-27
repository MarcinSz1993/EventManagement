package com.marcinsz.eventmanagementsystem.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Hour {
    @JsonProperty("time_epoch")
    private int timeEpoch;
    private String time;
    @JsonProperty("temp_c")
    private int tempC;
    @JsonProperty("temp_f")
    private int tempF;
    @JsonProperty("is_day")
    private int isDay;
    private Condition condition;
    @JsonProperty("wind_mph")
    private int windMph;
    @JsonProperty("wind_kph")
    private int windKph;
    @JsonProperty("wind_degree")
    private int windDegree;
    @JsonProperty("wind_dir")
    private String windDir;
    @JsonProperty("pressure_mb")
    private int pressureMb;
    @JsonProperty("pressure_in")
    private int pressureIn;
    @JsonProperty("precip_mm")
    private int precipMm;
    @JsonProperty("precip_in")
    private int precipIn;
    @JsonProperty("snow_cm")
    private int snowCm;
    private int humidity;
    private int cloud;
    @JsonProperty("feelslike_c")
    private int feelslikeC;
    @JsonProperty("feelslike_f")
    private int feelslikeF;
    @JsonProperty("windchill_c")
    private int windchillC;
    @JsonProperty("windchill_f")
    private int windchillF;
    @JsonProperty("heatindex_c")
    private int heatindexC;
    @JsonProperty("heatindex_f")
    private int heatindexF;
    @JsonProperty("dewpoint_c")
    private int dewpointC;
    @JsonProperty("dewpoint_f")
    private int dewpointF;
    @JsonProperty("will_it_rain")
    private int willItRain;
    @JsonProperty("chance_of_rain")
    private int chanceOfRain;
    @JsonProperty("will_it_snow")
    private int willItSnow;
    @JsonProperty("chance_of_snow")
    private int chanceOfSnow;
    @JsonProperty("vis_km")
    private int visKm;
    @JsonProperty("vis_miles")
    private int visMiles;
    @JsonProperty("gust_mph")
    private int gustMph;
    @JsonProperty("gust_kph")
    private int gustKph;
    private int uv;
}
