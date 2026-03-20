package ru.yandex.practicum.shoppingstore.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.interactionapi.api.ShoppingStoreOperations;
import ru.yandex.practicum.interactionapi.dto.store.ProductDto;
import ru.yandex.practicum.interactionapi.dto.store.SetProductQuantityStateRequest;
import ru.yandex.practicum.interactionapi.enums.ProductCategory;
import ru.yandex.practicum.shoppingstore.service.ShoppingStoreService;

import java.util.UUID;

@RestController
public class ShoppingStoreController implements ShoppingStoreOperations {

    private final ShoppingStoreService shoppingStoreService;

    public ShoppingStoreController(ShoppingStoreService shoppingStoreService) {
        this.shoppingStoreService = shoppingStoreService;
    }

    @Override
    public Page<ProductDto> getProducts(ProductCategory category, int page, int size, String[] sort) {
        PageRequest pageRequest;

        if (sort != null && sort.length > 0) {
            pageRequest = PageRequest.of(page, size, parseSort(sort));
        } else {
            pageRequest = PageRequest.of(page, size);
        }

        return shoppingStoreService.getProducts(category, pageRequest);
    }

    @Override
    public ProductDto getProductById(UUID productId) {
        return shoppingStoreService.getProductById(productId);
    }

    @Override
    public ProductDto createProduct(@Valid ProductDto productDto) {
        return shoppingStoreService.createProduct(productDto);
    }

    @Override
    public ProductDto updateProduct(@Valid ProductDto productDto) {
        return shoppingStoreService.updateProduct(productDto);
    }

    @Override
    public boolean removeProduct(UUID productId) {
        return shoppingStoreService.removeProduct(productId);
    }

    @Override
    public boolean setQuantityState(@Valid SetProductQuantityStateRequest request) {
        return shoppingStoreService.setQuantityState(request);
    }

    private Sort parseSort(String[] sortParams) {
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

        return sort;
    }
}