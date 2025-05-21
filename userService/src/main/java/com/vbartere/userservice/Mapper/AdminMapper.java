package com.vbartere.userservice.Mapper;

import com.vbartere.Shared.Kafka.DTO.AdminService.AdminUserDTO;
import com.vbartere.Shared.Kafka.Enum.UserEventType;
import com.vbartere.userservice.model.Role;
import com.vbartere.userservice.model.User;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class AdminMapper {
    public AdminUserDTO toDto(User user, UserEventType userEventType) {
        AdminUserDTO dto = new AdminUserDTO();

        dto.setId(user.getId());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        dto.setSurname(user.getSurname());
        dto.setInvitedByCode(user.getInvitedByCode());
        dto.setBanned(user.isBanned);
        dto.setEvent(userEventType);

        Set<Role> roles = user.getRoles();
        Set<String> roleNames = new HashSet<>();

        for (Role role : roles) {
            if (role != null && role.getName() != null) {
                roleNames.add(role.getName());
            }
        }

        dto.setRoles(roleNames);

        return dto;
    }
}
