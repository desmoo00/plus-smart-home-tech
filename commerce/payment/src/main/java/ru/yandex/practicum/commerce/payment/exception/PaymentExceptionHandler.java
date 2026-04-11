package ru.yandex.practicum.commerce.payment.exception;

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
public class PaymentExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(PaymentExceptionHandler.class);

    @ExceptionHandler(NotEnoughInfoInOrderToCalculateException.class)
    public ResponseEntity<Map<String, String>> handleNotEnoughInfo(NotEnoughInfoInOrderToCalculateException ex) {
        log.warn("Payment calculation request rejected: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(NoPaymentFoundException.class)
    public ResponseEntity<Map<String, String>> handleNoPayment(NoPaymentFoundException ex) {
        log.warn("Payment not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(FeignException.NotFound.class)
    public ResponseEntity<Map<String, String>> handleDownstreamNotFound(FeignException.NotFound ex) {
        log.warn("Downstream service did not find resource during payment flow: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        log.warn("Payment request validation failed: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(Map.of("message", "Invalid payment request"));
    }
}
