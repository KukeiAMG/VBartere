package com.example.Referral.kafka.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, String> consumerFactory(){
        Map <String,Object> Config = new HashMap<>();
        Config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,"localhost:9093");
        Config.put(ConsumerConfig.GROUP_ID_CONFIG,"referral-service-group");
        Config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class); //как будет десериализовать
        Config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(Config);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory <String, String> kafkaListenerContainerFactory(){
        ConcurrentKafkaListenerContainerFactory <String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }


}
