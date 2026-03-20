package ru.yandex.practicum.interactionapi.dto.store;

import java.util.List;

public record PageableObjectDto(
        long offset,
        List<SortObjectDto> sort,
        int pageNumber,
        int pageSize,
        boolean paged,
        boolean unpaged
) {
}