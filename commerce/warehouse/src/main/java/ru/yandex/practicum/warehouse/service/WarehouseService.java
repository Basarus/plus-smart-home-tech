package ru.yandex.practicum.warehouse.service;

import ru.yandex.practicum.interactionapi.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.interactionapi.dto.warehouse.*;

import java.util.Map;
import java.util.UUID;

public interface WarehouseService {

    void createProduct(NewProductInWarehouseRequest request);

    void addQuantity(AddProductToWarehouseRequest request);

    BookedProductsDto checkProducts(ShoppingCartDto shoppingCartDto);

    AddressDto getAddress();

    BookedProductsDto assemblyProductsForOrder(AssemblyProductsForOrderRequest request);

    void shippedToDelivery(ShippedToDeliveryRequest request);

    void acceptReturn(Map<UUID, Long> products);
}