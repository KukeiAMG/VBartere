package com.example.payment.kafka.producers;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
@Service
public class SendReferralCommission {
    private static final Logger log = LoggerFactory.getLogger(SendReferralCommission.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String TOPIC = "payment.calculatecomission.referral";

    public SendReferralCommission(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Async("taskExecutor")
    public void sendReferralCommission(String referralCommissionEventDTO) {
        try {

            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(TOPIC, referralCommissionEventDTO);

            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Ошибка при отправке сообщения в топик {}: {}", TOPIC, ex.getMessage());
                } else {
                    RecordMetadata metadata = result.getRecordMetadata();
                    log.info("Сообщение успешно отправлено в топик {}, partition: {}, offset: {}", 
                            metadata.topic(), metadata.partition(), metadata.offset());
                }
            });
        } catch (Exception e) {
            log.error("Ошибка при подготовке сообщения для отправки в топик {}: {}", TOPIC, e.getMessage());
        }
    }
}
