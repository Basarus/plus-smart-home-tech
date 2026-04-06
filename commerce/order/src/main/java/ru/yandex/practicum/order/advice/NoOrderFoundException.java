package ru.yandex.practicum.order.exception;

import java.util.UUID;

public class NoOrderFoundException extends RuntimeException {
    public NoOrderFoundException(UUID orderId) {
        super("Заказ не найден: " + orderId);
    }
}