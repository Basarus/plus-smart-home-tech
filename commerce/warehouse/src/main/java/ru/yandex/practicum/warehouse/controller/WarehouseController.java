package ru.yandex.practicum.warehouse.controller;

import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.interactionapi.api.WarehouseOperations;
import ru.yandex.practicum.interactionapi.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.interactionapi.dto.warehouse.*;
import ru.yandex.practicum.warehouse.service.WarehouseService;

import java.util.Map;
import java.util.UUID;

@RestController
public class WarehouseController implements WarehouseOperations {

    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @Override
    public void createProduct(NewProductInWarehouseRequest request) {
        warehouseService.createProduct(request);
    }

    @Override
    public void addQuantity(AddProductToWarehouseRequest request) {
        warehouseService.addQuantity(request);
    }

    @Override
    public BookedProductsDto checkProducts(ShoppingCartDto request) {
        return warehouseService.checkProducts(request);
    }

    @Override
    public AddressDto getAddress() {
        return warehouseService.getAddress();
    }

    @Override
    public BookedProductsDto assemblyProductsForOrder(AssemblyProductsForOrderRequest request) {
        return warehouseService.assemblyProductsForOrder(request);
    }

    @Override
    public void shippedToDelivery(ShippedToDeliveryRequest request) {
        warehouseService.shippedToDelivery(request);
    }

    @Override
    public void acceptReturn(Map<UUID, Long> products) {
        warehouseService.acceptReturn(products);
    }
}