package ru.yandex.practicum.shoppingcart.exception;

public class ShoppingCartDeactivatedException extends RuntimeException {
    public ShoppingCartDeactivatedException() {
        super("Корзина деактивирована");
    }
}