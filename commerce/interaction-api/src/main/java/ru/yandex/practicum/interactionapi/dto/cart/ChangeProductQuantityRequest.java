package ru.yandex.practicum.interactionapi.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ChangeProductQuantityRequest(
        @NotNull UUID productId,
        @Min(1) long newQuantity
) {
}