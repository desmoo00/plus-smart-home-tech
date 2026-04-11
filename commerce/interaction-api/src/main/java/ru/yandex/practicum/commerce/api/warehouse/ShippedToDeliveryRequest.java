package ru.yandex.practicum.commerce.api.warehouse;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ShippedToDeliveryRequest(
        @NotNull UUID orderId,
        @NotNull UUID deliveryId
) {
}
