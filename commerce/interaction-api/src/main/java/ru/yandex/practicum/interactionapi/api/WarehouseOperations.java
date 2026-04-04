package ru.yandex.practicum.interactionapi.api;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.interactionapi.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.interactionapi.dto.warehouse.*;

import java.util.Map;
import java.util.UUID;

public interface WarehouseOperations {

    @PutMapping("/api/v1/warehouse")
    void createProduct(@Valid @RequestBody NewProductInWarehouseRequest request);

    @PostMapping("/api/v1/warehouse/add")
    void addQuantity(@Valid @RequestBody AddProductToWarehouseRequest request);

    @PostMapping("/api/v1/warehouse/check")
    BookedProductsDto checkProducts(@Valid @RequestBody ShoppingCartDto request);

    @GetMapping("/api/v1/warehouse/address")
    AddressDto getAddress();

    BookedProductsDto assemblyProductsForOrder(AssemblyProductsForOrderRequest request);

    void shippedToDelivery(ShippedToDeliveryRequest request);

    void acceptReturn(Map<UUID, Long> products);
}