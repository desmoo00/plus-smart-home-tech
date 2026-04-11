package ru.yandex.practicum.commerce.cart.service;

import java.util.HashMap;
import ru.yandex.practicum.commerce.api.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.cart.model.ShoppingCart;

public final class ShoppingCartMapper {

    private ShoppingCartMapper() {
    }

    // Создаем DTO и копируем товары в новую Map, чтобы наружу не ушла внутренняя коллекция сущности.
    public static ShoppingCartDto toDto(ShoppingCart cart) {
        return new ShoppingCartDto(cart.getShoppingCartId(), new HashMap<>(cart.getProducts()));
    }
}
