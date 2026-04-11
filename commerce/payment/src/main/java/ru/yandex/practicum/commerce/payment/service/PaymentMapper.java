package ru.yandex.practicum.commerce.payment.service;

import java.util.UUID;
import ru.yandex.practicum.commerce.api.payment.PaymentDto;
import ru.yandex.practicum.commerce.api.payment.PaymentState;
import ru.yandex.practicum.commerce.payment.model.PaymentEntity;

public final class PaymentMapper {

    private PaymentMapper() {
    }

    public static PaymentEntity create(UUID orderId, PaymentDto dto) {
        PaymentEntity payment = new PaymentEntity();
        payment.setPaymentId(dto.paymentId());
        payment.setOrderId(orderId);
        payment.setProductTotal(dto.totalPayment().subtract(dto.deliveryTotal()).subtract(dto.feeTotal()));
        payment.setDeliveryTotal(dto.deliveryTotal());
        payment.setFeeTotal(dto.feeTotal());
        payment.setTotalPayment(dto.totalPayment());
        payment.setState(PaymentState.PENDING);
        return payment;
    }

    public static PaymentDto toDto(PaymentEntity payment) {
        return new PaymentDto(
                payment.getPaymentId(),
                payment.getTotalPayment(),
                payment.getDeliveryTotal(),
                payment.getFeeTotal()
        );
    }
}
