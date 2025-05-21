package com.vbartere.userservice.Kafka.Config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.*;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String BOOTSTRAP_SERVERS;

    @Bean
    public ProducerFactory<String, String> producerFactory() { // CartEvent
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() { // CartEvent
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public NewTopic newTopic() {
        return new NewTopic("cart-events", 1, (short) 1);
    }
//
//    @Bean
//    public ConsumerFactory<String, CartResult> consumerFactory() {
//        Map<String, Object> props = new HashMap<>();
//        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9093");
//        props.put(ConsumerConfig.GROUP_ID_CONFIG, "advertisement-group");
//        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.vbartere.Shared.Kafka");
//
//        return new DefaultKafkaConsumerFactory<>(props,
//                new StringDeserializer(),
//                new ErrorHandlingDeserializer<>(new JsonDeserializer<>(CartResult.class)));
//    }
//
//    @Bean
//    public ConcurrentKafkaListenerContainerFactory<String, CartResult> kafkaListenerContainerFactory() {
//        ConcurrentKafkaListenerContainerFactory<String, CartResult> factory =
//                new ConcurrentKafkaListenerContainerFactory<>();
//        factory.setConsumerFactory(consumerFactory());
//        return factory;
//    }
}