package com.vbartere.userservice.Mapper;

import com.vbartere.Shared.Kafka.DTO.AdminService.AdminUserDTO;
import com.vbartere.Shared.Kafka.DTO.Embeddable.CartItemDTO;
import com.vbartere.Shared.Kafka.Enum.UserEventType;
import com.vbartere.userservice.model.Cart;
import com.vbartere.userservice.model.Embeddable.CartItem;
import com.vbartere.userservice.model.Role;
import com.vbartere.userservice.model.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class AdminMapper {

    public AdminUserDTO toDto(User user, UserEventType userEventType) {
        AdminUserDTO dto = new AdminUserDTO();
        dto.setId(user.getId());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setName(user.getName());
        dto.setSurname(user.getSurname());
        dto.setInvitedByCode(user.getInvitedByCode());
        dto.setEmail(user.getEmail());
        dto.setBanned(user.isBanned);
        dto.setEvent(userEventType);

        if (user.getImage() != null) {
            dto.setImageUrl(user.getImage().getId().toString());
        }

        Set<Role> roles = user.getRoles();
        if (roles != null) {
            Set<String> roleNames = new HashSet<>();
            for (Role role : roles) {
                if (role != null && role.getName() != null) {
                    roleNames.add(role.getName());
                }
            }
            dto.setRoles(roleNames);
        }

        List<CartItemDTO> cartItemDTOs = new ArrayList<>();
        Cart cart = user.getCart();
        if (cart != null && cart.getAdvertisementList() != null) {
            for (CartItem item : cart.getAdvertisementList()) {
                if (item != null) {
                    CartItemDTO cartItemDTO = new CartItemDTO();
                    cartItemDTO.setAdvertisementId(item.getAdvertisementId());
                    cartItemDTO.setPrice(item.getPrice());
                    cartItemDTOs.add(cartItemDTO);
                }
            }
        }

        dto.setAddedAdvertisements(cartItemDTOs);
        return dto;
    }
}
