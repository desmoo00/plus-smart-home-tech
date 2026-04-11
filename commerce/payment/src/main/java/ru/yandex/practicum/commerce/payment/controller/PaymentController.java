package ru.yandex.practicum.commerce.payment.controller;

import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.api.order.OrderDto;
import ru.yandex.practicum.commerce.api.payment.PaymentClient;
import ru.yandex.practicum.commerce.api.payment.PaymentDto;
import ru.yandex.practicum.commerce.payment.service.PaymentService;

@RestController
public class PaymentController implements PaymentClient {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Override
    public PaymentDto payment(OrderDto order) {
        return paymentService.payment(order);
    }

    @Override
    public BigDecimal getTotalCost(OrderDto order) {
        return paymentService.getTotalCost(order);
    }

    @Override
    public void paymentSuccess(UUID paymentId) {
        paymentService.paymentSuccess(paymentId);
    }

    @Override
    public BigDecimal productCost(OrderDto order) {
        return paymentService.productCost(order);
    }

    @Override
    public void paymentFailed(UUID paymentId) {
        paymentService.paymentFailed(paymentId);
    }
}
