package com.marcinsz.eventmanagementsystem.service;

import com.marcinsz.eventmanagementsystem.configuration.KafkaConfig;
import com.marcinsz.eventmanagementsystem.dto.EventDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class KafkaMessageProducer {

    private final KafkaTemplate<String, EventDto> eventDtoKafkaTemplate;
    private final KafkaTemplate<String, EventDto> stringKafkaTemplate;
    private final KafkaConfig kafkaConfig;

    public void sendCreatedEventMessageToAllEventsTopic(EventDto eventDto){
        try {
            CompletableFuture<SendResult<String, EventDto>> allEvents = eventDtoKafkaTemplate.send(kafkaConfig.getAllEventsTopic(), eventDto);
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

    public void sendCancelledMessageToEventCancelledTopic(EventDto eventDto) {
        try {
            CompletableFuture<SendResult<String, EventDto>> cancelledEvents = stringKafkaTemplate.send(kafkaConfig.getCancelledEventsTopic(), eventDto);
            cancelledEvents.whenComplete((result, ex) -> {
                if (ex == null) {
                    System.out.println("Sent message: " + eventDto
                            + "with offset: " + result.getRecordMetadata().offset());
                } else {
                    System.out.println("Unable to send message: " + eventDto);
                }
            });
        } catch (Exception ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }
}
