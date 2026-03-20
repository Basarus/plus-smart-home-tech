package ru.yandex.practicum.interactionapi.dto.store;

public record SortObjectDto(
        String direction,
        String property,
        boolean ignoreCase,
        String nullHandling,
        boolean ascending,
        boolean descending
) {
}