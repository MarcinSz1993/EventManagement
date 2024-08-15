package com.marcinsz.eventmanagementsystem.csv;

import com.marcinsz.eventmanagementsystem.model.EventTarget;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventCsvRepresentation {
    @Column(unique = true)
    @CsvBindByName(column = "eventname")
    private String eventName;
    @CsvBindByName(column = "eventdescription")
    private String eventDescription;
    @CsvBindByName(column = "location")
    private String location;
    @CsvBindByName(column = "maxattendees")
    private int maxAttendees;
    @CsvBindByName(column = "eventdate")
    @CsvDate(value = "yyyy-MM-dd")
    private LocalDate eventDate;
    @CsvBindByName(column = "ticketprice")
    private double ticketPrice;
    @CsvBindByName(column = "eventtarget")
    private EventTarget eventTarget;
}
