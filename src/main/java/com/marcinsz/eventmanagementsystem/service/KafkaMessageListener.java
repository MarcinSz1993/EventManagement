package com.marcinsz.eventmanagementsystem.service;

import com.marcinsz.eventmanagementsystem.dto.EventDto;
import com.marcinsz.eventmanagementsystem.model.EventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KafkaMessageListener {
    @KafkaListener(topics = "allEvents", groupId = "events-group")
    public void consumeMessage(EventDto eventDto){
        if(eventDto.getEventType().equals(EventType.CHILDREN)){
            log.info(String.valueOf(eventDto));
        } else {
            log.info("Event different than for children.");
        }
    }
}
