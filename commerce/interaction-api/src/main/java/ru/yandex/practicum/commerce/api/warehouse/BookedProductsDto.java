package ru.yandex.practicum.commerce.api.warehouse;

import jakarta.validation.constraints.NotNull;

public record BookedProductsDto(
        @NotNull Double deliveryWeight,
        @NotNull Double deliveryVolume,
        @NotNull Boolean fragile
) {
}
