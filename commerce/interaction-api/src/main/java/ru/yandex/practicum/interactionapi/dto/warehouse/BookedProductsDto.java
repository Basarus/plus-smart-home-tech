package ru.yandex.practicum.interactionapi.dto.warehouse;

public record BookedProductsDto(
        double deliveryWeight,
        double deliveryVolume,
        boolean fragile
) {
}