package ru.yandex.practicum.shoppingcart.client;

import ru.yandex.practicum.interactionapi.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.interactionapi.dto.warehouse.BookedProductsDto;

public interface WarehouseGateway {
    BookedProductsDto checkProducts(ShoppingCartDto shoppingCartDto);
}