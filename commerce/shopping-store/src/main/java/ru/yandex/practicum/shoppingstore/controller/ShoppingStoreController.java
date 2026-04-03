package ru.yandex.practicum.shoppingstore.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.interactionapi.api.ShoppingStoreOperations;
import ru.yandex.practicum.interactionapi.dto.store.PageProductDto;
import ru.yandex.practicum.interactionapi.dto.store.ProductDto;
import ru.yandex.practicum.interactionapi.dto.store.SetProductQuantityStateRequest;
import ru.yandex.practicum.interactionapi.enums.ProductCategory;
import ru.yandex.practicum.interactionapi.enums.QuantityState;
import ru.yandex.practicum.shoppingstore.mapper.PageProductMapper;
import ru.yandex.practicum.shoppingstore.service.ShoppingStoreService;

import java.util.UUID;

@RestController
public class ShoppingStoreController implements ShoppingStoreOperations {

    private static final int MAX_PRODUCTS_PAGE_SIZE = 1000;

    private final ShoppingStoreService shoppingStoreService;
    private final PageProductMapper pageProductMapper;

    public ShoppingStoreController(ShoppingStoreService shoppingStoreService,
                                   PageProductMapper pageProductMapper) {
        this.shoppingStoreService = shoppingStoreService;
        this.pageProductMapper = pageProductMapper;
    }

    @Override
    public PageProductDto getProducts(ProductCategory category, int page, int size, String[] sort) {
        PageRequest pageRequest = PageRequest.of(page, size, parseSort(sort));
        Page<ProductDto> productsPage = shoppingStoreService.getProducts(category, pageRequest);
        return pageProductMapper.toDto(productsPage);
    }

    @Override
    public ProductDto getProductById(UUID productId) {
        return shoppingStoreService.getProductById(productId);
    }

    @Override
    public ProductDto createProduct(ProductDto productDto) {
        return shoppingStoreService.createProduct(productDto);
    }

    @Override
    public ProductDto updateProduct(ProductDto productDto) {
        return shoppingStoreService.updateProduct(productDto);
    }

    @Override
    public ProductDto removeProduct(UUID productId) {
        return shoppingStoreService.removeProduct(productId);
    }

    @Override
    public ProductDto setQuantityState(@RequestParam UUID productId,
                                       @RequestParam QuantityState quantityState) {
        return shoppingStoreService.setQuantityState(
                new SetProductQuantityStateRequest(productId, quantityState)
        );
    }

    private Sort parseSort(String[] sortParams) {
        if (sortParams == null || sortParams.length == 0) {
            return Sort.by(Sort.Direction.ASC, "productName");
        }

        if (sortParams.length == 1) {
            String raw = sortParams[0];
            if (raw == null || raw.isBlank()) {
                return Sort.by(Sort.Direction.ASC, "productName");
            }

            String[] parts = raw.split(",");
            String property = parts[0].trim();
            Sort.Direction direction = Sort.Direction.ASC;

            if (parts.length > 1) {
                direction = Sort.Direction.fromOptionalString(parts[1].trim().toUpperCase())
                        .orElse(Sort.Direction.ASC);
            }

            return Sort.by(direction, property);
        }

        if (sortParams.length == 2 &&
                !sortParams[0].contains(",") &&
                !sortParams[1].contains(",")) {
            String property = sortParams[0].trim();
            Sort.Direction direction = Sort.Direction.fromOptionalString(sortParams[1].trim().toUpperCase())
                    .orElse(Sort.Direction.ASC);
            return Sort.by(direction, property);
        }

        Sort sort = Sort.unsorted();
        for (String sortParam : sortParams) {
            if (sortParam == null || sortParam.isBlank()) {
                continue;
            }

            String[] parts = sortParam.split(",");
            String property = parts[0].trim();
            Sort.Direction direction = Sort.Direction.ASC;

            if (parts.length > 1) {
                direction = Sort.Direction.fromOptionalString(parts[1].trim().toUpperCase())
                        .orElse(Sort.Direction.ASC);
            }

            sort = sort.and(Sort.by(direction, property));
        }

        return sort.isSorted() ? sort : Sort.by(Sort.Direction.ASC, "productName");
    }
}