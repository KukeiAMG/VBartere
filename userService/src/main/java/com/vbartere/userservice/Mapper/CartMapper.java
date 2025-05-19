package com.vbartere.userservice.Mapper;

import com.vbartere.Shared.Kafka.DTO.Cart.CartDTO;
import com.vbartere.userservice.model.Cart;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class CartMapper {
    public CartDTO toDto(Cart cart) {
        if (cart == null) {
            return null;
        }

        CartDTO dto = new CartDTO();
        dto.setId(cart.getId());

        if (cart.getUser() != null) {
            dto.setUserId(cart.getUser().getId());
        }

        if (cart.getAdvertisementList() != null) {
            dto.setAdvertisementIds(new ArrayList<>(cart.getAdvertisementList()));
        } else {
            dto.setAdvertisementIds(new ArrayList<>());
        }

        return dto;
    }
}
