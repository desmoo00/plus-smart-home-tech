package ru.yandex.practicum.commerce.delivery.controller;

import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.api.delivery.DeliveryClient;
import ru.yandex.practicum.commerce.api.delivery.DeliveryDto;
import ru.yandex.practicum.commerce.api.order.OrderDto;
import ru.yandex.practicum.commerce.delivery.service.DeliveryService;

@RestController
public class DeliveryController implements DeliveryClient {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @Override
    public DeliveryDto planDelivery(DeliveryDto delivery) {
        return deliveryService.planDelivery(delivery);
    }

    @Override
    public void deliverySuccessful(UUID orderId) {
        deliveryService.deliverySuccessful(orderId);
    }

    @Override
    public void deliveryPicked(UUID orderId) {
        deliveryService.deliveryPicked(orderId);
    }

    @Override
    public void deliveryFailed(UUID orderId) {
        deliveryService.deliveryFailed(orderId);
    }

    @Override
    public BigDecimal deliveryCost(OrderDto order) {
        return deliveryService.deliveryCost(order);
    }
}
