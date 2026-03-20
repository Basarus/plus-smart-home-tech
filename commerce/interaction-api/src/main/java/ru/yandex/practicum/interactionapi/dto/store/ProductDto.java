package ru.yandex.practicum.interactionapi.dto.store;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ru.yandex.practicum.interactionapi.enums.ProductCategory;
import ru.yandex.practicum.interactionapi.enums.ProductState;
import ru.yandex.practicum.interactionapi.enums.QuantityState;

import java.util.UUID;

public record ProductDto(
        UUID productId,
        @NotBlank String productName,
        @NotBlank String description,
        @NotBlank String imageSrc,
        @NotNull QuantityState quantityState,
        @NotNull ProductState productState,
        @NotNull ProductCategory productCategory,
        @NotNull Double price
) {
}