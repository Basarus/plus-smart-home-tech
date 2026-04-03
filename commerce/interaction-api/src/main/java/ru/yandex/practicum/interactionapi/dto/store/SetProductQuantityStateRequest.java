package ru.yandex.practicum.interactionapi.dto.store;

import jakarta.validation.constraints.NotNull;
import ru.yandex.practicum.interactionapi.enums.QuantityState;

import java.util.UUID;

public record SetProductQuantityStateRequest(
        @NotNull UUID productId,
        @NotNull QuantityState quantityState
) {
}