package ru.yandex.practicum.commerce.api.store;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record ProductDto(
        UUID productId,
        @NotBlank String productName,
        @NotBlank String description,
        String imageSrc,
        @NotNull QuantityState quantityState,
        @NotNull ProductState productState,
        @NotNull ProductCategory productCategory,
        @NotNull @DecimalMin("1.0") BigDecimal price
) {
}
