package ru.yandex.practicum.interactionapi.dto.warehouse;

public record AddressDto(
        String country,
        String city,
        String street,
        String house,
        String flat
) {
}