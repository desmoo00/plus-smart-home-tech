package ru.yandex.practicum.commerce.payment.exception;

public class NotEnoughInfoInOrderToCalculateException extends RuntimeException {

    public NotEnoughInfoInOrderToCalculateException(String message) {
        super(message);
    }
}
