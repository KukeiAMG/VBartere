package com.example.payment.kafka.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.producer.key-serializer}")
    private String keySerializer;

    @Value("${spring.kafka.producer.value-serializer}")
    private String valueSerializer;

    @Bean
    public ProducerFactory<String, String> producerFactory() { // CartEvent
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all"); // Гарантированная доставка
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3); // Количество попыток
        configProps.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000); // Задержка между попытками
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true); // Идемпотентность
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384); // Размер батча
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 1); // Задержка для батчинга
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432); // Размер буфера
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() { // CartEvent
        return new KafkaTemplate<>(producerFactory());
    }
}