package ru.yandex.practicum.interactionapi.api;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.interactionapi.dto.store.ProductDto;
import ru.yandex.practicum.interactionapi.enums.ProductCategory;
import ru.yandex.practicum.interactionapi.enums.QuantityState;

import java.util.UUID;

@RequestMapping("/api/v1/shopping-store")
public interface ShoppingStoreOperations {

    @GetMapping
    Page<ProductDto> getProducts(@RequestParam(required = false) ProductCategory category,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size,
                                 @RequestParam(required = false) String[] sort);

    @GetMapping("/{productId}")
    ProductDto getProductById(@PathVariable UUID productId);

    @PutMapping
    ProductDto createProduct(@RequestBody ProductDto productDto);

    @PostMapping
    ProductDto updateProduct(@RequestBody ProductDto productDto);

    @PostMapping("/removeProductFromStore")
    ProductDto removeProduct(@RequestBody UUID productId);

    @PostMapping("/quantityState")
    ProductDto setQuantityState(@RequestParam UUID productId,
                                @RequestParam QuantityState quantityState);
}