package ru.yandex.practicum.interactionapi.dto.warehouse;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record NewProductInWarehouseRequest(
        @NotNull UUID productId,
        @NotNull Boolean fragile,
        @NotNull DimensionDto dimension,
        Double weight,
        @Min(1) int quantity
) {
}