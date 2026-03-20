package ru.yandex.practicum.warehouse.controller;

import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.interactionapi.api.WarehouseOperations;
import ru.yandex.practicum.interactionapi.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.interactionapi.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.interactionapi.dto.warehouse.AddressDto;
import ru.yandex.practicum.interactionapi.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.interactionapi.dto.warehouse.NewProductInWarehouseRequest;
import ru.yandex.practicum.warehouse.service.WarehouseService;

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
}