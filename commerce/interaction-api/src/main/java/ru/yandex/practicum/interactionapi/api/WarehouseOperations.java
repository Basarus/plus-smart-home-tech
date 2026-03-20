package ru.yandex.practicum.interactionapi.api;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.interactionapi.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.interactionapi.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.interactionapi.dto.warehouse.AddressDto;
import ru.yandex.practicum.interactionapi.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.interactionapi.dto.warehouse.NewProductInWarehouseRequest;

@RequestMapping("/api/v1/warehouse")
public interface WarehouseOperations {

    @PutMapping
    void createProduct(@Valid @RequestBody NewProductInWarehouseRequest request);

    @PostMapping("/add")
    void addQuantity(@Valid @RequestBody AddProductToWarehouseRequest request);

    @PostMapping("/check")
    BookedProductsDto checkProducts(@Valid @RequestBody ShoppingCartDto request);

    @GetMapping("/address")
    AddressDto getAddress();
}