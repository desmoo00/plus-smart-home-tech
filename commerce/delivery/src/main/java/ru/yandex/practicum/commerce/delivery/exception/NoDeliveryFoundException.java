package ru.yandex.practicum.commerce.delivery.exception;

import java.util.UUID;

public class NoDeliveryFoundException extends RuntimeException {

    public NoDeliveryFoundException(UUID orderId) {
        super("Delivery for order " + orderId + " not found");
    }
}
