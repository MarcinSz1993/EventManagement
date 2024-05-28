package com.marcinsz.eventmanagementsystem.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Astro {
    private String sunrise;
    private String sunset;
    private String moonrise;
    private String moonset;
    @JsonProperty("moon_phase")
    private String moonPhase;
    @JsonProperty("moon_illumination")
    private int moonIllumination;
    @JsonProperty("is_moon_up")
    private int isMoonUp;
    @JsonProperty("is_sun_up")
    private int isSunUp;
}
