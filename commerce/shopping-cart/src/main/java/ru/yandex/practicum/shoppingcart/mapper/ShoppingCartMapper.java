package ru.yandex.practicum.shoppingcart.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.interactionapi.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.shoppingcart.model.ShoppingCart;
import ru.yandex.practicum.shoppingcart.model.ShoppingCartItem;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ShoppingCartMapper {

    public ShoppingCartDto toDto(ShoppingCart cart) {
        Map<UUID, Long> products = cart.getItems().stream()
                .collect(Collectors.toMap(
                        ShoppingCartItem::getProductId,
                        ShoppingCartItem::getQuantity
                ));

        return new ShoppingCartDto(cart.getShoppingCartId(), products);
    }
}