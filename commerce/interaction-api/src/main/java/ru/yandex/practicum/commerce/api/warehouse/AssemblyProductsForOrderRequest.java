package ru.yandex.practicum.commerce.api.warehouse;

import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;

public record AssemblyProductsForOrderRequest(
        @NotNull Map<UUID, Long> products,
        @NotNull UUID orderId
) {
}
