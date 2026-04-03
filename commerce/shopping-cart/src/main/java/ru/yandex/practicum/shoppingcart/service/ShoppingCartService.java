package ru.yandex.practicum.shoppingcart.service;

import ru.yandex.practicum.interactionapi.dto.cart.ChangeProductQuantityRequest;
import ru.yandex.practicum.interactionapi.dto.cart.ShoppingCartDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ShoppingCartService {
    ShoppingCartDto getShoppingCart(String username);

    ShoppingCartDto addProductToShoppingCart(String username, Map<UUID, Long> products);

    void deactivateCurrentShoppingCart(String username);

    ShoppingCartDto removeFromShoppingCart(String username, List<UUID> products);

    ShoppingCartDto changeProductQuantity(String username, ChangeProductQuantityRequest request);
}