package ru.yandex.practicum.commerce.cart.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.api.cart.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.api.cart.ShoppingCartClient;
import ru.yandex.practicum.commerce.api.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.cart.service.ShoppingCartService;

@RestController
public class ShoppingCartController implements ShoppingCartClient {

    private final ShoppingCartService shoppingCartService;

    public ShoppingCartController(ShoppingCartService shoppingCartService) {
        this.shoppingCartService = shoppingCartService;
    }

    @Override
    public ShoppingCartDto getShoppingCart(String username) {
        return shoppingCartService.getCart(username);
    }

    @Override
    public ShoppingCartDto addProductToShoppingCart(String username, Map<UUID, Long> products) {
        return shoppingCartService.addProducts(username, products);
    }

    @Override
    public void deactivateCurrentShoppingCart(String username) {
        shoppingCartService.deactivate(username);
    }

    @Override
    public ShoppingCartDto removeFromShoppingCart(String username, List<UUID> productIds) {
        return shoppingCartService.removeProducts(username, productIds);
    }

    @Override
    public ShoppingCartDto changeProductQuantity(String username, @Valid ChangeProductQuantityRequest request) {
        return shoppingCartService.changeQuantity(username, request);
    }
}
