package ru.yandex.practicum.commerce.api.warehouse;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record DimensionDto(
        @NotNull @DecimalMin("1.0") Double width,
        @NotNull @DecimalMin("1.0") Double height,
        @NotNull @DecimalMin("1.0") Double depth
) {
}
