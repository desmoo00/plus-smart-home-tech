package ru.yandex.practicum.commerce.order.exception;

import feign.FeignException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class OrderExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(OrderExceptionHandler.class);

    @ExceptionHandler(NotAuthorizedUserException.class)
    public ResponseEntity<Map<String, String>> handleUnauthorized(NotAuthorizedUserException ex) {
        log.warn("Order request rejected because username is invalid: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(NoOrderFoundException.class)
    public ResponseEntity<Map<String, String>> handleNoOrder(NoOrderFoundException ex) {
        log.warn("Order was not found: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(FeignException.BadRequest.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(FeignException.BadRequest ex) {
        log.warn("Downstream service returned bad request for order flow: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(FeignException.NotFound.class)
    public ResponseEntity<Map<String, String>> handleNotFound(FeignException.NotFound ex) {
        log.warn("Downstream service did not find requested data for order flow: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        log.warn("Order request validation failed: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(Map.of("message", "Invalid order request"));
    }
}
