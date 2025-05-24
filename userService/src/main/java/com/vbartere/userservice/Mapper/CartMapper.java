package com.vbartere.userservice.Mapper;

import com.vbartere.Shared.Kafka.DTO.Cart.CartDTO;
import com.vbartere.Shared.Kafka.DTO.Embeddable.CartItemDTO;
import com.vbartere.userservice.model.Cart;
import com.vbartere.userservice.model.Embeddable.CartItem;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CartMapper {
    public CartDTO toDto(Cart cart) {
        if (cart == null) {
            return null;
        }

        CartDTO dto = new CartDTO();
        dto.setId(cart.getId());

        // Маппинг пользователя
        if (cart.getUser() != null) {
            dto.setUserId(cart.getUser().getId());
        }

        // Маппинг списка товаров
        if (cart.getAdvertisementList() != null) {
            List<CartItemDTO> itemDTOs = new ArrayList<>();
            for (CartItem item : cart.getAdvertisementList()) {
                itemDTOs.add(toCartItemDto(item));
            }
            dto.setAdvertisementIds(itemDTOs);
        } else {
            dto.setAdvertisementIds(new ArrayList<>());
        }

        return dto;
    }

    private CartItemDTO toCartItemDto(CartItem item) {
        CartItemDTO dto = new CartItemDTO();
        dto.setAdvertisementId(item.getAdvertisementId());
        dto.setPrice(item.getPrice());
        dto.setSelected(item.getSelected());
        return dto;
    }
}
