package com.vbartere.AdminService.Kafka.Service.Consumers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vbartere.AdminService.Model.AdminUser;
import com.vbartere.AdminService.Repository.AdminUserRepository;
import com.vbartere.Shared.Kafka.DTO.AdminService.AdminUserDTO;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdminUserEventConsumer {

    private final AdminUserRepository adminUserRepository;
    private final ObjectMapper objectMapper;

    public AdminUserEventConsumer(AdminUserRepository adminUserRepository, ObjectMapper objectMapper) {
        this.adminUserRepository = adminUserRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "administration.user.event")
    public void handleUserEvent(String message) throws JsonProcessingException {
        System.out.println(message);

        AdminUserDTO event = objectMapper.readValue(message, AdminUserDTO.class);

        switch (event.getEvent()) {
            case USER_CREATED, USER_UPDATED -> {
                Optional<AdminUser> optionalUser = adminUserRepository.findById(event.getId());
                AdminUser adminUser;

                if (optionalUser.isPresent()) {
                    adminUser = optionalUser.get();
                } else {
                    adminUser = new AdminUser();
                    adminUser.setId(event.getId());
                }

                adminUser.setPhoneNumber(event.getPhoneNumber());
                adminUser.setName(event.getName());
                adminUser.setSurname(event.getSurname());
                adminUser.setInvitedByCode(event.getInvitedByCode());
                adminUser.setEmail(event.getEmail());
                adminUser.setImageUrl(event.getImageUrl());
                adminUser.setRoles(event.getRoles());
                adminUser.setBanned(event.isBanned());

                adminUserRepository.save(adminUser);

                System.out.println(adminUser);
            }
            case USER_DELETED -> { adminUserRepository.deleteById(event.getId()); }
        }
    }
}
