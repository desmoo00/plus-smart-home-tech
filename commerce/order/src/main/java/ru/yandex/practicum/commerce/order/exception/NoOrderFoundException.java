package ru.yandex.practicum.commerce.order.exception;

import java.util.UUID;

public class NoOrderFoundException extends RuntimeException {

    public NoOrderFoundException(UUID orderId) {
        super("Order " + orderId + " not found");
    }
}
