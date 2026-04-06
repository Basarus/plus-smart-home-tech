package ru.yandex.practicum.interactionapi.dto.delivery;

import ru.yandex.practicum.interactionapi.dto.warehouse.AddressDto;
import ru.yandex.practicum.interactionapi.enums.DeliveryState;

import java.util.UUID;

public record DeliveryDto(
        UUID deliveryId,
        AddressDto fromAddress,
        AddressDto toAddress,
        UUID orderId,
        DeliveryState deliveryState
) {
}