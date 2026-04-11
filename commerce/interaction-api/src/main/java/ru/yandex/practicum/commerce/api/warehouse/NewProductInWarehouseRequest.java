package ru.yandex.practicum.commerce.api.warehouse;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record NewProductInWarehouseRequest(
        @NotNull UUID productId,
        boolean fragile,
        @NotNull DimensionDto dimension,
        @NotNull @DecimalMin("1.0") Double weight
) {
}
