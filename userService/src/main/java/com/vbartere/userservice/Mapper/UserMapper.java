package com.vbartere.userservice.Mapper;

import com.vbartere.Shared.Kafka.DTO.UserService.UserDTO;
import com.vbartere.userservice.model.Role;
import com.vbartere.userservice.model.User;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class UserMapper {

    public UserDTO userToDTO(User user) {
        if (user == null) {
            return null;
        }

        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setName(user.getName());
        dto.setSurname(user.getSurname());
        dto.setEmail(user.getEmail());
        dto.setInvitedByCode(user.getInvitedByCode());
        dto.setBanned(user.isBanned);

        // Установка imageUrl из image.filePath, если есть изображение
        if (user.getImage() != null) {
            dto.setImageUrl(user.getImage().getFilePath());
        }

        if (user.getRoles() != null) {
            Set<String> roleNames = new HashSet<>();
            for (Role role : user.getRoles()) {
                roleNames.add(role.getName());
            }
            dto.setRoles(roleNames);
        }

        return dto;
    }
}
