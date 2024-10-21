package com.marcinsz.eventmanagementsystem.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;

@Data
@Component
@AllArgsConstructor
@NoArgsConstructor
public class Cart {
    private LinkedHashMap<String, Double> events;

    public void addTicket(Event event) {
        events.put(event.getEventName(), event.getTicketPrice());
    }

    public void removeTicket(Event event) {
        events.remove(event.getEventName(), event.getTicketPrice());
    }

    public double getTotalPrice() {
        return events.values()
                .stream()
                .mapToDouble(Double::doubleValue)
                .sum();
    }
}
