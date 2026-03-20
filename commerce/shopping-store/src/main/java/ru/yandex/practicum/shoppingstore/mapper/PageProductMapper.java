package ru.yandex.practicum.shoppingstore.mapper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.interactionapi.dto.store.PageProductDto;
import ru.yandex.practicum.interactionapi.dto.store.PageableObjectDto;
import ru.yandex.practicum.interactionapi.dto.store.ProductDto;
import ru.yandex.practicum.interactionapi.dto.store.SortObjectDto;

import java.util.List;

@Component
public class PageProductMapper {

    public PageProductDto toDto(Page<ProductDto> page) {
        return new PageProductDto(
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.getSize(),
                page.getContent(),
                page.getNumber(),
                mapSort(page.getSort()),
                page.getNumberOfElements(),
                mapPageable(page.getPageable()),
                page.isEmpty()
        );
    }

    private PageableObjectDto mapPageable(Pageable pageable) {
        return new PageableObjectDto(
                pageable.getOffset(),
                mapSort(pageable.getSort()),
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.isPaged(),
                pageable.isUnpaged()
        );
    }

    private List<SortObjectDto> mapSort(Sort sort) {
        return sort.stream()
                .map(this::mapOrder)
                .toList();
    }

    private SortObjectDto mapOrder(Sort.Order order) {
        return new SortObjectDto(
                order.getDirection().name(),
                order.getProperty(),
                order.isIgnoreCase(),
                order.getNullHandling().name(),
                order.isAscending(),
                order.isDescending()
        );
    }
}