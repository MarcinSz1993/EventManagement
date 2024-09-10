package com.marcinsz.eventmanagementsystem.kafka;

public interface KafkaMessageSender<T> {
    void sendMessage(String topic, T message);
}
