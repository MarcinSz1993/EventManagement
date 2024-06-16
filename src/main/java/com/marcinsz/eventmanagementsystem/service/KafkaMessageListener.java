package com.marcinsz.eventmanagementsystem.service;

import com.marcinsz.eventmanagementsystem.dto.EventDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KafkaMessageListener {
    @KafkaListener(topics = "allEvents", groupId = "events-group")
    public void consumeMessage(EventDto eventDto){
        log.info(String.valueOf(eventDto));
    }
}
