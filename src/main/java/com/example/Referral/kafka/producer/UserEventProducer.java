//package com.example.Referral.kafka.producer;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.stereotype.Component;
//
//@Component
//public class UserEventProducer {
//    private final KafkaTemplate <String,String> kafkaTemplate;
//    private final ObjectMapper objectMapper;
//
//    public UserEventProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
//        this.kafkaTemplate = kafkaTemplate;
//        this.objectMapper = objectMapper;
//    }
//
//    // Метод для отправки пользовательского события
//    public void sendUserEvent(UserReferralDTO user){
//        try{
//            // Сериализация DTO в JSON и отправка в топик submit-user
//            // NOTE
//            //  Отсутствует обработка ошибок отправки в Kafka
//            //  Нет метрик для мониторинга отправки сообщений
//            //  Жестко закодированное имя топика ("submit-user")
//            if (user == null) {
//                throw new IllegalArgumentException("UserEventDTO cannot be null");
//            }
//
//            kafkaTemplate.send("submit-user", objectMapper.writeValueAsString(user));// отправляю сериализованного пользователя в submit-user
//        }catch(JsonProcessingException e){
//            System.err.println("Ошибка сериализации: " + e.getMessage());
//            // TODO
//            //  Добавить логирование и метрики ошибок
//        }
//    }
//}
