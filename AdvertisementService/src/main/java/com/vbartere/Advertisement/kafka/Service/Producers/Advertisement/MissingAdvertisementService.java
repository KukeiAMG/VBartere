package com.vbartere.Advertisement.kafka.Service.Producers.Advertisement;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class MissingAdvertisementService {
    private final KafkaTemplate<String, String> kafkaTemplate;

    private final String TOPIC = "missing.advertisements";

    public MissingAdvertisementService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Async("taskExecutor")
    public void sendMissingAdvertisementRequest(String cartResult) {
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(TOPIC, cartResult);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                System.err.println("ошибка при отправке сообщения: " + ex.getMessage());
            } else {
                RecordMetadata metadata = result.getRecordMetadata();
                System.out.printf("сообщение отправлено в ",
                        metadata.topic(), metadata.partition(), metadata.offset());
            }
        });
    }
}
