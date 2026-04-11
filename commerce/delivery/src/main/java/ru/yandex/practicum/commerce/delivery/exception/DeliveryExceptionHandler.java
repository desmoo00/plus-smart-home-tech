package ru.yandex.practicum.commerce.delivery.exception;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class DeliveryExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(DeliveryExceptionHandler.class);

    @ExceptionHandler(NoDeliveryFoundException.class)
    public ResponseEntity<Map<String, String>> handleNoDelivery(NoDeliveryFoundException ex) {
        log.warn("Delivery not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        log.warn("Delivery request validation failed: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(Map.of("message", "Invalid delivery request"));
    }
}
