package com.marcinsz.eventmanagementsystem.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Current {
    private int last_updated_epoch;
    @JsonProperty("last_updated")
    private String lastUpdated;
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
    @JsonProperty("precip_mm") //Precipitation amount in millimeters
    private int precipMm;
    @JsonProperty("precip_in")
    private int precipIn;
    private int humidity;
    private int cloud;
    @JsonProperty("feelslike_c")
    private int feelslikeC;
    @JsonProperty("feelslike_f")
    private int feelslikeF;
    @JsonProperty("vis_km")
    private int visKm;
    @JsonProperty("vis_miles")
    private int visMiles;
    private int uv;
    @JsonProperty("gust_mph")
    private int gustMph;
    @JsonProperty("gust_kph")
    private int gustKph;
}
