package ru.yandex.practicum.interactionapi.api;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.interactionapi.dto.store.ProductDto;
import ru.yandex.practicum.interactionapi.dto.store.SetProductQuantityStateRequest;
import ru.yandex.practicum.interactionapi.enums.ProductCategory;

import java.util.UUID;

@RequestMapping("/api/v1/shopping-store")
public interface ShoppingStoreOperations {

    @GetMapping
    Page<ProductDto> getProducts(@RequestParam ProductCategory category,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size,
                                 @RequestParam(required = false) String[] sort);

    @GetMapping("/{productId}")
    ProductDto getProductById(@PathVariable UUID productId);

    @PutMapping
    ProductDto createProduct(@Valid @RequestBody ProductDto productDto);

    @PostMapping
    ProductDto updateProduct(@Valid @RequestBody ProductDto productDto);

    @PostMapping("/removeProductFromStore")
    boolean removeProduct(@RequestBody UUID productId);

    @PostMapping("/quantityState")
    boolean setQuantityState(@Valid @RequestBody SetProductQuantityStateRequest request);
}