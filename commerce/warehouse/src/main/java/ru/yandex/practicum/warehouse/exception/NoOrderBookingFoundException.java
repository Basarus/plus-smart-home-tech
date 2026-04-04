package ru.yandex.practicum.warehouse.exception;

import java.util.UUID;

public class NoOrderBookingFoundException extends RuntimeException {
    public NoOrderBookingFoundException(UUID orderId) {
        super("Для заказа с id " + orderId + " не найдено бронирование на складе");
    }
}