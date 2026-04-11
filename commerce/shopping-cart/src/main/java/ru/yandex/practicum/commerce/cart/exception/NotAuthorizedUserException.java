package ru.yandex.practicum.commerce.cart.exception;

public class NotAuthorizedUserException extends RuntimeException {

    public NotAuthorizedUserException() {
        super("Username must not be blank");
    }
}
