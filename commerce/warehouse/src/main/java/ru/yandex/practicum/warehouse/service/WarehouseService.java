package ru.yandex.practicum.warehouse.service;

import ru.yandex.practicum.interactionapi.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.interactionapi.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.interactionapi.dto.warehouse.AddressDto;
import ru.yandex.practicum.interactionapi.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.interactionapi.dto.warehouse.NewProductInWarehouseRequest;

public interface WarehouseService {

    void createProduct(NewProductInWarehouseRequest request);

    void addQuantity(AddProductToWarehouseRequest request);

    BookedProductsDto checkProducts(ShoppingCartDto shoppingCartDto);

    AddressDto getAddress();
}