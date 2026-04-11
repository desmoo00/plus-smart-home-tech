package ru.yandex.practicum.commerce.order.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;
import ru.yandex.practicum.commerce.api.order.CreateNewOrderRequest;
import ru.yandex.practicum.commerce.api.order.OrderDto;
import ru.yandex.practicum.commerce.api.order.OrderState;
import ru.yandex.practicum.commerce.api.warehouse.AddressDto;
import ru.yandex.practicum.commerce.order.model.OrderAddress;
import ru.yandex.practicum.commerce.order.model.OrderEntity;

public final class OrderMapper {

    private OrderMapper() {
    }

    public static OrderEntity fromRequest(CreateNewOrderRequest request) {
        OrderEntity order = new OrderEntity();
        order.setOrderId(UUID.randomUUID());
        order.setShoppingCartId(request.shoppingCart().shoppingCartId());
        order.setProducts(new HashMap<>(request.shoppingCart().products()));
        order.setState(OrderState.NEW);
        order.setCreatedAt(LocalDateTime.now());
        order.setDeliveryAddress(toAddress(request.deliveryAddress()));
        return order;
    }

    public static OrderDto toDto(OrderEntity order) {
        return new OrderDto(
                order.getOrderId(),
                order.getShoppingCartId(),
                new HashMap<>(order.getProducts()),
                order.getPaymentId(),
                order.getDeliveryId(),
                order.getState(),
                order.getDeliveryWeight(),
                order.getDeliveryVolume(),
                order.getFragile(),
                order.getTotalPrice(),
                order.getDeliveryPrice(),
                order.getProductPrice()
        );
    }

    public static AddressDto toAddressDto(OrderAddress address) {
        return new AddressDto(
                address.getCountry(),
                address.getCity(),
                address.getStreet(),
                address.getHouse(),
                address.getFlat()
        );
    }

    private static OrderAddress toAddress(AddressDto dto) {
        OrderAddress address = new OrderAddress();
        address.setCountry(dto.country());
        address.setCity(dto.city());
        address.setStreet(dto.street());
        address.setHouse(dto.house());
        address.setFlat(dto.flat());
        return address;
    }
}
