package ru.yandex.practicum.commerce.api.order;

import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;

public record ProductReturnRequest(
        UUID orderId,
        @NotNull Map<UUID, Long> products
) {
}
