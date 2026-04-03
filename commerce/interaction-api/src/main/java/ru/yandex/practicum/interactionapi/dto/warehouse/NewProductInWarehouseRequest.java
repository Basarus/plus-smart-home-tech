package ru.yandex.practicum.interactionapi.dto.warehouse;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record NewProductInWarehouseRequest(
        @NotNull UUID productId,
        Boolean fragile,
        @NotNull DimensionDto dimension,
        @NotNull Double weight
) {
}