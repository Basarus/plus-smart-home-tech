package ru.yandex.practicum.warehouse.exception;

import java.util.UUID;

public class ProductInShoppingCartLowQuantityInWarehouseException extends RuntimeException {
    public ProductInShoppingCartLowQuantityInWarehouseException(UUID productId, long requested, long available) {
        super("Недостаточно товара на складе. productId=" + productId +
                ", requested=" + requested +
                ", available=" + available);
    }
}