package ru.yandex.practicum.commerce.cart.exception;

public class WarehouseUnavailableException extends RuntimeException {

    public WarehouseUnavailableException(Throwable cause) {
        super("Warehouse is unavailable, product availability cannot be checked", cause);
    }
}
