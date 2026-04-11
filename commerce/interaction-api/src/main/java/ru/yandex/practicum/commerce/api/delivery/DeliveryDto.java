package ru.yandex.practicum.commerce.api.delivery;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import ru.yandex.practicum.commerce.api.warehouse.AddressDto;

public record DeliveryDto(
        @NotNull UUID deliveryId,
        @NotNull @Valid AddressDto fromAddress,
        @NotNull @Valid AddressDto toAddress,
        @NotNull UUID orderId,
        @NotNull DeliveryState deliveryState
) {
}
