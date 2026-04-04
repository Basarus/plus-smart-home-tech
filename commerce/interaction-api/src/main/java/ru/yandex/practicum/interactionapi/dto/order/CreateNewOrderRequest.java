package ru.yandex.practicum.interactionapi.dto.order;

import ru.yandex.practicum.interactionapi.dto.warehouse.AddressDto;

import java.util.UUID;

public record CreateNewOrderRequest(
        UUID shoppingCartId,
        String username,
        AddressDto toAddress
) {
}