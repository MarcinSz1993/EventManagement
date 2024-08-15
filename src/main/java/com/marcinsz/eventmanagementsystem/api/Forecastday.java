package com.marcinsz.eventmanagementsystem.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Forecastday {
    private String date;
    @JsonProperty("date_epoch")
    private int dateEpoch;
    private Day day;
    private Astro astro;
    private List<Hour> hour;
}
