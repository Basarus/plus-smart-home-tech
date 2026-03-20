package ru.yandex.practicum.warehouse.exception;

import java.util.UUID;

public class NoSpecifiedProductInWarehouseException extends RuntimeException {
    public NoSpecifiedProductInWarehouseException(UUID productId) {
        super("Нет информации о товаре с id " + productId + " на складе");
    }
}