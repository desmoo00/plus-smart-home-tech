package ru.yandex.practicum.commerce.order.controller;

import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.api.order.CreateNewOrderRequest;
import ru.yandex.practicum.commerce.api.order.OrderClient;
import ru.yandex.practicum.commerce.api.order.OrderDto;
import ru.yandex.practicum.commerce.api.order.ProductReturnRequest;
import ru.yandex.practicum.commerce.order.service.OrderService;

@RestController
public class OrderController implements OrderClient {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public List<OrderDto> getClientOrders(String username) {
        return orderService.getClientOrders(username);
    }

    @Override
    public OrderDto createNewOrder(CreateNewOrderRequest request) {
        return orderService.createNewOrder(request);
    }

    @Override
    public OrderDto productReturn(ProductReturnRequest request) {
        return orderService.productReturn(request);
    }

    @Override
    public OrderDto payment(UUID orderId) {
        return orderService.payment(orderId);
    }

    @Override
    public OrderDto paymentFailed(UUID orderId) {
        return orderService.paymentFailed(orderId);
    }

    @Override
    public OrderDto delivery(UUID orderId) {
        return orderService.delivery(orderId);
    }

    @Override
    public OrderDto deliveryFailed(UUID orderId) {
        return orderService.deliveryFailed(orderId);
    }

    @Override
    public OrderDto complete(UUID orderId) {
        return orderService.complete(orderId);
    }

    @Override
    public OrderDto calculateTotalCost(UUID orderId) {
        return orderService.calculateTotalCost(orderId);
    }

    @Override
    public OrderDto calculateDeliveryCost(UUID orderId) {
        return orderService.calculateDeliveryCost(orderId);
    }

    @Override
    public OrderDto assembly(UUID orderId) {
        return orderService.assembly(orderId);
    }

    @Override
    public OrderDto assemblyFailed(UUID orderId) {
        return orderService.assemblyFailed(orderId);
    }
}
