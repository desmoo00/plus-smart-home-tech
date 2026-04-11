package ru.yandex.practicum.commerce.warehouse.exception;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class WarehouseExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(WarehouseExceptionHandler.class);

    // Логируем попытку повторно зарегистрировать товар, который уже есть на складе.
    @ExceptionHandler(SpecifiedProductAlreadyInWarehouseException.class)
    public ResponseEntity<Map<String, String>> handleAlreadyExists(SpecifiedProductAlreadyInWarehouseException ex) {
        log.warn("Товар уже зарегистрирован на складе: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }

    // Логируем ошибки, связанные с отсутствием товара или нехваткой остатка на складе.
    @ExceptionHandler({
            NoSpecifiedProductInWarehouseException.class,
            ProductInShoppingCartLowQuantityInWarehouseException.class
    })
    public ResponseEntity<Map<String, String>> handleBadWarehouseRequest(RuntimeException ex) {
        log.warn("Ошибка запроса к складу: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }

    // Логируем ошибки валидации, чтобы было видно, почему запрос к складу отклонен.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        log.warn("Ошибка валидации запроса склада: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Invalid warehouse request"));
    }
}
