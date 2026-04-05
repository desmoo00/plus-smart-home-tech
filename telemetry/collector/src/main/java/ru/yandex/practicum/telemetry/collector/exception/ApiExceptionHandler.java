package ru.yandex.practicum.telemetry.collector.exception;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableJson(HttpMessageNotReadableException exception) {
        return ResponseEntity.badRequest().body(errorBody("Bad request body"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        // собираем ошибки по полям, чтобы клиенту было проще понять причину валидации
        Map<String, String> fields = new HashMap<>();

        for (FieldError error : exception.getBindingResult().getFieldErrors()) {
            fields.put(error.getField(), error.getDefaultMessage());
        }

        return ResponseEntity.badRequest().body(errorBody("Validation failed", fields));
    }

    @ExceptionHandler(KafkaPublishException.class)
    public ResponseEntity<ErrorResponse> handleKafkaPublish(KafkaPublishException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorBody("Could not save event to Kafka"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorBody("Unexpected server error"));
    }

    private ErrorResponse errorBody(String message) {
        return new ErrorResponse(message);
    }

    private ErrorResponse errorBody(String message, Map<String, String> fields) {
        return new ErrorResponse(message, fields);
    }

    public static class ErrorResponse {

        private final String message;
        private final Map<String, String> fields;

        public ErrorResponse(String message) {
            this(message, null);
        }

        public ErrorResponse(String message, Map<String, String> fields) {
            this.message = message;
            this.fields = fields;
        }

        public String getMessage() {
            return message;
        }

        public Map<String, String> getFields() {
            return fields;
        }
    }
}
