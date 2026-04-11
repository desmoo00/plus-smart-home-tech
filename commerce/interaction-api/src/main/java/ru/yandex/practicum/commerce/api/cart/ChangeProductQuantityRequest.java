package ru.yandex.practicum.commerce.api.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ChangeProductQuantityRequest(
        @NotNull UUID productId,
        @NotNull @Min(1) Long newQuantity
) {
}
