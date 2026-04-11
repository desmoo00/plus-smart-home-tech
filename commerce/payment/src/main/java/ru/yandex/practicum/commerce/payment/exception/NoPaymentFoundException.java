package ru.yandex.practicum.commerce.payment.exception;

import java.util.UUID;

public class NoPaymentFoundException extends RuntimeException {

    public NoPaymentFoundException(UUID paymentId) {
        super("Payment " + paymentId + " not found");
    }
}
