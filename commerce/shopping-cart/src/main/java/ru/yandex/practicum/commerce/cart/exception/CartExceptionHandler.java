package ru.yandex.practicum.commerce.cart.exception;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CartExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(CartExceptionHandler.class);

    // Логируем ситуацию, когда пользователь обращается к корзине без корректного имени.
    @ExceptionHandler(NotAuthorizedUserException.class)
    public ResponseEntity<Map<String, String>> handleUnauthorized(NotAuthorizedUserException ex) {
        log.warn("Ошибка авторизации при работе с корзиной: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", ex.getMessage()));
    }

    // Логируем случай, когда в корзине не найден нужный товар для операции.
    @ExceptionHandler(NoProductsInShoppingCartException.class)
    public ResponseEntity<Map<String, String>> handleNoProducts(NoProductsInShoppingCartException ex) {
        log.warn("Ошибка работы с корзиной: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }

    // Логируем ошибки, когда корзина не может проверить остатки через склад.
    @ExceptionHandler(WarehouseUnavailableException.class)
    public ResponseEntity<Map<String, String>> handleWarehouseUnavailable(WarehouseUnavailableException ex) {
        log.warn("Склад недоступен при обработке корзины", ex);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("message", ex.getMessage()));
    }

    // Логируем ошибки валидации, чтобы было видно, почему запрос не прошел проверку.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        log.warn("Ошибка валидации запроса корзины: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(Map.of("message", "Invalid shopping cart request"));
    }
}
