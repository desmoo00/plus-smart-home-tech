package ru.yandex.practicum.commerce.cart.service;

import java.util.HashMap;
import ru.yandex.practicum.commerce.api.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.cart.model.ShoppingCart;

final class ShoppingCartMapper {

    private ShoppingCartMapper() {
    }

    static ShoppingCartDto toDto(ShoppingCart cart) {
        return new ShoppingCartDto(cart.getShoppingCartId(), new HashMap<>(cart.getProducts()));
    }
}
