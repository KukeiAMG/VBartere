package com.vbartere.AdminService.Mapper;

import com.vbartere.AdminService.Model.AdminUser;
import com.vbartere.Shared.Kafka.DTO.AdminService.AdminUserDTO;
import org.springframework.stereotype.Component;

@Component
public class AdminUserMapper {
    public AdminUserDTO toDTO(AdminUser entity) {
        if (entity == null) return null;

        AdminUserDTO dto = new AdminUserDTO();
        dto.setId(entity.getId());
        dto.setPhoneNumber(entity.getPhoneNumber());
        dto.setName(entity.getName());
        dto.setSurname(entity.getSurname());
        dto.setInvitedByCode(entity.getInvitedByCode());
        dto.setEmail(entity.getEmail());
        dto.setImageUrl(entity.getImageUrl());
        dto.setRoles(entity.getRoles());
        dto.setBanned(entity.isBanned());

        return dto;
    }
}
