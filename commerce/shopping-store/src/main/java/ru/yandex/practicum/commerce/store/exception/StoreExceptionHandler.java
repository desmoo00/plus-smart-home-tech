package ru.yandex.practicum.commerce.store.exception;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class StoreExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(StoreExceptionHandler.class);

    // Логируем ситуацию, когда магазин не нашел товар по переданному идентификатору.
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleProductNotFound(ProductNotFoundException ex) {
        log.warn("Товар не найден: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
    }

    // Логируем ошибки валидации, чтобы было проще понять причину отклонения запроса.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        log.warn("Ошибка валидации запроса магазина: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(Map.of("message", "Invalid product request"));
    }
}
