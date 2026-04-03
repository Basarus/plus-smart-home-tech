package ru.yandex.practicum.interactionapi.dto.cart;

import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public record ShoppingCartDto(
        @NotNull UUID shoppingCartId,
        @NotNull Map<UUID, Long> products
) {
}