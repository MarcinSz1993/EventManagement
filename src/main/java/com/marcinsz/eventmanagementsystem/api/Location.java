package com.marcinsz.eventmanagementsystem.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Location {
        private String name;
        private String region;
        private String country;
        private int lat;
        private int lon;
        @JsonProperty("tz_id") //Time zone
        private String tzId;
        @JsonProperty("localtime_epoch")
        private int localtimeEpoch;
        private String localtime;
    }