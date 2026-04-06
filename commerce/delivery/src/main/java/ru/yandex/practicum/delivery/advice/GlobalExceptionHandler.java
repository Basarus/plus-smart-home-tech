package ru.yandex.practicum.delivery.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.delivery.exception.NoDeliveryFoundException;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoDeliveryFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNoDeliveryFound(NoDeliveryFoundException e) {
        log.warn("Доставка не найдена: {}", e.getMessage(), e);
        return Map.of("error", e.getMessage());
    }
}