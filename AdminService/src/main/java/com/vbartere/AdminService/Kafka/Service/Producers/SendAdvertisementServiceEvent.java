package com.vbartere.AdminService.Kafka.Service.Producers;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class SendAdvertisementServiceEvent {

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final String TOPIC = "advertisement.event";

    public SendAdvertisementServiceEvent(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Async("taskExecutor")
    public void sendAdvertisementRequest(String advertisementRequest) {
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(TOPIC, advertisementRequest);

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
