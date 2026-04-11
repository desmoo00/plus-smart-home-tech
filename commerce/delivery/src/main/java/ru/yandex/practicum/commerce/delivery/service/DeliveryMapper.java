package ru.yandex.practicum.commerce.delivery.service;

import ru.yandex.practicum.commerce.api.delivery.DeliveryDto;
import ru.yandex.practicum.commerce.api.warehouse.AddressDto;
import ru.yandex.practicum.commerce.delivery.model.DeliveryAddress;
import ru.yandex.practicum.commerce.delivery.model.DeliveryEntity;

public final class DeliveryMapper {

    private DeliveryMapper() {
    }

    public static DeliveryEntity toEntity(DeliveryDto dto) {
        DeliveryEntity entity = new DeliveryEntity();
        entity.setDeliveryId(dto.deliveryId());
        entity.setOrderId(dto.orderId());
        entity.setDeliveryState(dto.deliveryState());
        entity.setFromAddress(toAddress(dto.fromAddress()));
        entity.setToAddress(toAddress(dto.toAddress()));
        return entity;
    }

    public static DeliveryDto toDto(DeliveryEntity entity) {
        return new DeliveryDto(
                entity.getDeliveryId(),
                toDto(entity.getFromAddress()),
                toDto(entity.getToAddress()),
                entity.getOrderId(),
                entity.getDeliveryState()
        );
    }

    private static DeliveryAddress toAddress(AddressDto dto) {
        DeliveryAddress address = new DeliveryAddress();
        address.setCountry(dto.country());
        address.setCity(dto.city());
        address.setStreet(dto.street());
        address.setHouse(dto.house());
        address.setFlat(dto.flat());
        return address;
    }

    private static AddressDto toDto(DeliveryAddress address) {
        return new AddressDto(
                address.getCountry(),
                address.getCity(),
                address.getStreet(),
                address.getHouse(),
                address.getFlat()
        );
    }
}
