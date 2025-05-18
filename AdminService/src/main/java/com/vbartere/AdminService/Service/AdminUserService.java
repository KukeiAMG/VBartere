package com.vbartere.AdminService.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vbartere.AdminService.Kafka.Service.Producers.SendAdvertisementServiceEvent;
import com.vbartere.AdminService.Kafka.Service.Producers.SendUserServiceEvent;
import com.vbartere.AdminService.Mapper.AdminUserMapper;
import com.vbartere.AdminService.Model.AdminUser;
import com.vbartere.AdminService.Repository.AdminAdvertisementRepository;
import com.vbartere.AdminService.Repository.AdminUserRepository;
import com.vbartere.Shared.Kafka.DTO.AdminService.AdminUserDTO;
import com.vbartere.Shared.Kafka.Enum.UserEventType;
import com.vbartere.Shared.Kafka.Events.UserEvent;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminUserService {

    private final AdminUserRepository adminUserRepository;
    private final ObjectMapper objectMapper;
    private final SendUserServiceEvent sendUserServiceEvent;
    private final AdminUserMapper adminUserMapper;

    public AdminUserService(AdminUserRepository adminUserRepository, ObjectMapper objectMapper, SendUserServiceEvent sendUserServiceEvent, AdminUserMapper adminUserMapper) {
        this.adminUserRepository = adminUserRepository;
        this.objectMapper = objectMapper;
        this.sendUserServiceEvent = sendUserServiceEvent;
        this.adminUserMapper = adminUserMapper;
    }

    @Transactional(readOnly = true)
    public AdminUserDTO getById(Long id) {
        AdminUser entity = adminUserRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Пользователь не найден в БД")
        );

        return adminUserMapper.toDTO(entity);
    }

    @Transactional(readOnly = true)
    public List<AdminUserDTO> getAll() {
        List<AdminUser> users = adminUserRepository.findAll();
        List<AdminUserDTO> dtoList = new ArrayList<>();
        for (AdminUser user : users) {
            AdminUserDTO dto = adminUserMapper.toDTO(user);
            dtoList.add(dto);
        }

        return dtoList;
    }

    @Transactional
    public void banUser(Long id, String description) throws JsonProcessingException {
        AdminUser adminUser = adminUserRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Пользователь не найден в БД")
        );
        adminUser.setBanned(true);
        adminUserRepository.save(adminUser);

        UserEvent userEvent = new UserEvent();
        userEvent.setName(adminUser.getName());
        userEvent.setEmail(adminUser.getEmail());
        userEvent.setEvent(UserEventType.USER_BANNED);
        userEvent.setDescription(description);

        sendUserServiceEvent.sendUserRequest(objectMapper.writeValueAsString(userEvent));
    }
}
