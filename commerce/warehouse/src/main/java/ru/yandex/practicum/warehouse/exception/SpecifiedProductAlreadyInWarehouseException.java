package ru.yandex.practicum.warehouse.exception;

import java.util.UUID;

public class SpecifiedProductAlreadyInWarehouseException extends RuntimeException {
    public SpecifiedProductAlreadyInWarehouseException(UUID productId) {
        super("Товар с id " + productId + " уже зарегистрирован на складе");
    }
}