package com.vbartere.Advertisement.kafka;

import com.vbartere.Shared.Kafka.CartEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {

    @KafkaListener(topics = "cart-events", groupId = "advertisement-group",
            containerFactory = "kafkaListenerContainerFactory")
    public void handleCartEvent(CartEvent event) {
        // Логика обработки события
        System.out.println("Обработка события: " + event);
    }
}
