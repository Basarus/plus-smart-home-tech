package ru.yandex.practicum.shoppingcart.advice;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.shoppingcart.exception.NoProductsInShoppingCartException;
import ru.yandex.practicum.shoppingcart.exception.NotAuthorizedUserException;
import ru.yandex.practicum.shoppingcart.exception.ShoppingCartDeactivatedException;
import ru.yandex.practicum.shoppingcart.exception.WarehouseUnavailableException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotAuthorizedUserException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> handleUnauthorized(NotAuthorizedUserException e) {
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(NoProductsInShoppingCartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleNoProducts(NoProductsInShoppingCartException e) {
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(ShoppingCartDeactivatedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleDeactivated(ShoppingCartDeactivatedException e) {
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(WarehouseUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Map<String, String> handleWarehouseUnavailable(WarehouseUnavailableException e) {
        return Map.of("error", e.getMessage());
    }
}