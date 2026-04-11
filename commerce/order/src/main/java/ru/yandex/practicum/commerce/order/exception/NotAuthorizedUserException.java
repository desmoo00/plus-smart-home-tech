package ru.yandex.practicum.commerce.order.exception;

public class NotAuthorizedUserException extends RuntimeException {

    public NotAuthorizedUserException() {
        super("Username must not be blank");
    }
}
