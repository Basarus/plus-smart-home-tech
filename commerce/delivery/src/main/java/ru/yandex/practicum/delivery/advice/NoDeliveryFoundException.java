package ru.yandex.practicum.delivery.exception;

import java.util.UUID;

public class NoDeliveryFoundException extends RuntimeException {
    public NoDeliveryFoundException(UUID deliveryId) {
        super("Доставка не найдена: " + deliveryId);
    }
}