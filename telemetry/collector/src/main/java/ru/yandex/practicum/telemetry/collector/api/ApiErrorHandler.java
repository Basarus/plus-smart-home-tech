package ru.yandex.practicum.telemetry.collector.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ApiErrorHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handle(Exception e) {
        return ResponseEntity.status(500).body(Map.of(
                "error", e.getClass().getName(),
                "message", String.valueOf(e.getMessage())
        ));
    }
}