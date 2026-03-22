package ru.yandex.practicum.shoppingcart.advice;

import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotAuthorizedUserException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> handleUnauthorized(NotAuthorizedUserException e) {
        log.warn("Unauthorized access to shopping cart: {}", e.getMessage());
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(NoProductsInShoppingCartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleNoProducts(NoProductsInShoppingCartException e) {
        log.warn("Shopping cart operation failed: {}", e.getMessage());
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(ShoppingCartDeactivatedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleDeactivated(ShoppingCartDeactivatedException e) {
        log.warn("Shopping cart is deactivated: {}", e.getMessage());
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(WarehouseUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Map<String, String> handleWarehouseUnavailable(WarehouseUnavailableException e) {
        log.error("Warehouse service unavailable: {}", e.getMessage(), e);
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidation(MethodArgumentNotValidException e) {
        log.warn("Validation error in shopping cart request: {}", e.getMessage());
        return Map.of("error", "Некорректные входные данные");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Illegal argument in shopping cart request: {}", e.getMessage());
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleOther(Exception e) {
        log.error("Unexpected error in shopping cart", e);
        return Map.of("error", e.getMessage() == null ? "Internal server error" : e.getMessage());
    }
}