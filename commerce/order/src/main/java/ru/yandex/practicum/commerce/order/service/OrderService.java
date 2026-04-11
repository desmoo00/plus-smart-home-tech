package ru.yandex.practicum.commerce.order.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.api.delivery.DeliveryClient;
import ru.yandex.practicum.commerce.api.delivery.DeliveryDto;
import ru.yandex.practicum.commerce.api.delivery.DeliveryState;
import ru.yandex.practicum.commerce.api.order.CreateNewOrderRequest;
import ru.yandex.practicum.commerce.api.order.OrderDto;
import ru.yandex.practicum.commerce.api.order.OrderState;
import ru.yandex.practicum.commerce.api.order.ProductReturnRequest;
import ru.yandex.practicum.commerce.api.payment.PaymentClient;
import ru.yandex.practicum.commerce.api.payment.PaymentDto;
import ru.yandex.practicum.commerce.api.warehouse.AddressDto;
import ru.yandex.practicum.commerce.api.warehouse.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.commerce.api.warehouse.BookedProductsDto;
import ru.yandex.practicum.commerce.api.warehouse.WarehouseClient;
import ru.yandex.practicum.commerce.order.exception.NoOrderFoundException;
import ru.yandex.practicum.commerce.order.exception.NotAuthorizedUserException;
import ru.yandex.practicum.commerce.order.model.OrderEntity;
import ru.yandex.practicum.commerce.order.repository.OrderRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final DeliveryClient deliveryClient;
    private final PaymentClient paymentClient;
    private final WarehouseClient warehouseClient;

    public OrderService(
            OrderRepository orderRepository,
            DeliveryClient deliveryClient,
            PaymentClient paymentClient,
            WarehouseClient warehouseClient
    ) {
        this.orderRepository = orderRepository;
        this.deliveryClient = deliveryClient;
        this.paymentClient = paymentClient;
        this.warehouseClient = warehouseClient;
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getClientOrders(String username) {
        validateUsername(username);
        List<OrderEntity> orders = orderRepository.findAllByUsernameOrderByCreatedAtDesc(username);
        if (orders.isEmpty()) {
            orders = orderRepository.findAllByOrderByCreatedAtDesc();
        }
        return orders.stream()
                .map(OrderMapper::toDto)
                .toList();
    }

    @Transactional
    public OrderDto createNewOrder(CreateNewOrderRequest request) {
        OrderEntity order = OrderMapper.fromRequest(request);
        order.setCreatedAt(LocalDateTime.now());
        return saveOrder(order);
    }

    @Transactional
    public OrderDto productReturn(ProductReturnRequest request) {
        OrderEntity order = getOrder(request.orderId());
        warehouseClient.acceptReturn(request.products());
        return saveOrderWithState(order, OrderState.PRODUCT_RETURNED);
    }

    @Transactional
    public OrderDto payment(UUID orderId) {
        OrderEntity order = getOrder(orderId);
        if (order.getPaymentId() != null) {
            return saveOrderWithState(order, OrderState.PAID);
        }

        ensureDeliveryPrice(order);
        ensureOrderPrices(order);

        PaymentDto payment = paymentClient.payment(OrderMapper.toDto(order));
        order.setPaymentId(payment.paymentId());
        order.setState(OrderState.ON_PAYMENT);
        return saveOrder(order);
    }

    @Transactional
    public OrderDto paymentFailed(UUID orderId) {
        return saveOrderWithState(getOrder(orderId), OrderState.PAYMENT_FAILED);
    }

    @Transactional
    public OrderDto delivery(UUID orderId) {
        return saveOrderWithState(getOrder(orderId), OrderState.DELIVERED);
    }

    @Transactional
    public OrderDto deliveryFailed(UUID orderId) {
        return saveOrderWithState(getOrder(orderId), OrderState.DELIVERY_FAILED);
    }

    @Transactional
    public OrderDto complete(UUID orderId) {
        return saveOrderWithState(getOrder(orderId), OrderState.COMPLETED);
    }

    @Transactional
    public OrderDto calculateTotalCost(UUID orderId) {
        OrderEntity order = getOrder(orderId);
        ensureDeliveryPrice(order);
        ensureOrderPrices(order);
        return saveOrder(order);
    }

    @Transactional
    public OrderDto calculateDeliveryCost(UUID orderId) {
        OrderEntity order = getOrder(orderId);
        ensureDeliveryPrice(order);
        return saveOrder(order);
    }

    @Transactional
    public OrderDto assembly(UUID orderId) {
        OrderEntity order = getOrder(orderId);
        ensureAssemblyData(order);
        return saveOrderWithState(order, OrderState.ASSEMBLED);
    }

    @Transactional
    public OrderDto assemblyFailed(UUID orderId) {
        return saveOrderWithState(getOrder(orderId), OrderState.ASSEMBLY_FAILED);
    }

    // Если доставка уже привязана к заказу — ничего не делаем.
    // Иначе создаём новую доставку и сохраняем её id в заказ.
    private void planDeliveryIfNeeded(OrderEntity order) {
        if (order.getDeliveryId() != null) {
            return;
        }

        AddressDto warehouseAddress = warehouseClient.getWarehouseAddress();
        DeliveryDto plannedDelivery = deliveryClient.planDelivery(new DeliveryDto(
                UUID.randomUUID(),
                warehouseAddress,
                OrderMapper.toAddressDto(order.getDeliveryAddress()),
                order.getOrderId(),
                DeliveryState.CREATED
        ));
        order.setDeliveryId(plannedDelivery.deliveryId());
    }

    // Гарантирует, что доставка существует и её стоимость уже посчитана.
    private void ensureDeliveryPrice(OrderEntity order) {
        planDeliveryIfNeeded(order);
        BigDecimal deliveryCost = deliveryClient.deliveryCost(OrderMapper.toDto(order));
        order.setDeliveryPrice(deliveryCost);
    }

    // Пересчитывает стоимость товаров и итоговую сумму заказа
    private void ensureOrderPrices(OrderEntity order) {
        BigDecimal productCost = paymentClient.productCost(OrderMapper.toDto(order));
        order.setProductPrice(productCost);
        BigDecimal totalCost = paymentClient.getTotalCost(OrderMapper.toDto(order));
        order.setTotalPrice(totalCost);
    }

    // Если параметры доставки уже есть - не трогаем.
    // Иначе резервируем товары и получаем вес, объём и хрупкость.
    private void ensureAssemblyData(OrderEntity order) {
        if (order.getDeliveryWeight() != null && order.getDeliveryVolume() != null && order.getFragile() != null) {
            return;
        }

        BookedProductsDto bookedProducts = warehouseClient.assemblyProductsForOrder(
                new AssemblyProductsForOrderRequest(order.getProducts(), order.getOrderId())
        );
        order.setDeliveryWeight(bookedProducts.deliveryWeight());
        order.setDeliveryVolume(bookedProducts.deliveryVolume());
        order.setFragile(bookedProducts.fragile());
    }

    // Упрощает смену статуса: обновили статус - сразу сохранили заказ
    private OrderDto saveOrderWithState(OrderEntity order, OrderState state) {
        order.setState(state);
        return saveOrder(order);
    }

    // Сохраняет заказ и возвращает DTO для дальнейшего использования
    private OrderDto saveOrder(OrderEntity order) {
        return OrderMapper.toDto(orderRepository.save(order));
    }

    private OrderEntity getOrder(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException(orderId));
    }

    private void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new NotAuthorizedUserException();
        }
    }
}
