package ru.yandex.practicum.commerce.api.payment;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentDto(
        UUID paymentId,
        BigDecimal totalPayment,
        BigDecimal deliveryTotal,
        BigDecimal feeTotal
) {
}
