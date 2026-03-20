package ru.yandex.practicum.shoppingstore.exception;

import java.util.UUID;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(UUID productId) {
        super("Товар с id " + productId + " не найден");
    }
}