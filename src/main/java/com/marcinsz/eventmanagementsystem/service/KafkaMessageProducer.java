package com.marcinsz.eventmanagementsystem.service;

import com.marcinsz.eventmanagementsystem.dto.EventDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class KafkaMessageProducer {

    private final KafkaTemplate<String, EventDto> template;

    public void sendMessageToTopic(EventDto eventDto){
        try {
            CompletableFuture<SendResult<String, EventDto>> allEvents = template.send("allEvents", eventDto);
            allEvents.whenComplete((result, ex) -> {
                if (ex == null) {
                    System.out.println("Sent message: " + eventDto.toString() +
                            "with offset: " + result.getRecordMetadata().offset());
                } else {
                    System.out.println("Unable to send message: " + eventDto.toString());
                }
            });
        }catch (Exception ex){
            System.out.println("Error: " + ex.getMessage());
        }
    }
}
