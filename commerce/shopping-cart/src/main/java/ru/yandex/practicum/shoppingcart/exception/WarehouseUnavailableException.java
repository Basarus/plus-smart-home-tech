package ru.yandex.practicum.shoppingcart.exception;

public class WarehouseUnavailableException extends RuntimeException {
    public WarehouseUnavailableException() {
        super("Сервис склада временно недоступен. Попробуйте позже.");
    }
}