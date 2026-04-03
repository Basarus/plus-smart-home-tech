package ru.yandex.practicum.shoppingcart.advice;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidation(MethodArgumentNotValidException e) {
        return Map.of("error", "Некорректные входные данные");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleIllegalArgument(IllegalArgumentException e) {
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleOther(Exception e) {
        return Map.of("error", e.getMessage() == null ? "Internal server error" : e.getMessage());
    }
}