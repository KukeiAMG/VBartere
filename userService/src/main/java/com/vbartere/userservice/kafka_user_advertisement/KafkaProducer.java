package com.vbartere.userservice.kafka_user_advertisement;

import com.vbartere.Shared.Kafka.CartEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {

    private final KafkaTemplate<String, CartEvent> kafkaTemplate;

    public KafkaProducer(KafkaTemplate<String, CartEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String topic, CartEvent event) {
        kafkaTemplate.send(
                topic,
                event
        );
    }

}
