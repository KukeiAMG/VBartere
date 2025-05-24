package com.vbartere.userservice.Kafka.Producers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class SendPaymentRequest {

    private final KafkaTemplate<String, String> kafkaTemplate;

    final String TOPIC = "user.payment.event";

    public SendPaymentRequest(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Async("taskExecutor")
    public void sendRequest(String UserEvent) {
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(TOPIC, UserEvent);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                System.err.println("ошибка при отправке сообщения: " + ex.getMessage());
            } else {
                RecordMetadata metadata = result.getRecordMetadata();
                System.out.println("Сообщение отправлено в " + metadata.topic() +
                        ", partition: " + metadata.partition() +
                        ", offset: " + metadata.offset());
            }
        });
    }
}
