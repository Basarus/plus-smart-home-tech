package ru.yandex.practicum.interactionapi.dto.warehouse;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ShippedToDeliveryRequest(
        @NotNull UUID orderId,
        @NotNull UUID deliveryId
) {
}