package ru.yandex.practicum.commerce.api.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import ru.yandex.practicum.commerce.api.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.api.warehouse.AddressDto;

public record CreateNewOrderRequest(
        @NotNull @Valid ShoppingCartDto shoppingCart,
        @NotNull @Valid AddressDto deliveryAddress
) {
}
