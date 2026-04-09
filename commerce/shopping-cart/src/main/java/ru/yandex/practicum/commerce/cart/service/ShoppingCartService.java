package ru.yandex.practicum.commerce.cart.service;

import feign.FeignException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.cloud.client.circuitbreaker.NoFallbackAvailableException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.api.cart.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.api.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.api.warehouse.WarehouseClient;
import ru.yandex.practicum.commerce.cart.exception.NoProductsInShoppingCartException;
import ru.yandex.practicum.commerce.cart.exception.NotAuthorizedUserException;
import ru.yandex.practicum.commerce.cart.exception.WarehouseUnavailableException;
import ru.yandex.practicum.commerce.cart.model.ShoppingCart;
import ru.yandex.practicum.commerce.cart.repository.ShoppingCartRepository;

@Service
public class ShoppingCartService {

    private final ShoppingCartRepository cartRepository;
    private final WarehouseClient warehouseClient;

    public ShoppingCartService(ShoppingCartRepository cartRepository, WarehouseClient warehouseClient) {
        this.cartRepository = cartRepository;
        this.warehouseClient = warehouseClient;
    }

    @Transactional
    public ShoppingCartDto getCart(String username) {
        validateUsername(username);
        return ShoppingCartMapper.toDto(getOrCreateActiveCart(username));
    }

    @Transactional
    public ShoppingCartDto addProducts(String username, Map<UUID, Long> products) {
        validateUsername(username);
        ShoppingCart cart = getOrCreateActiveCart(username);
        products.forEach((productId, quantity) -> {
            if (quantity != null && quantity > 0) {
                cart.getProducts().merge(productId, quantity, Long::sum);
            }
        });
        verifyWarehouseAvailability(cart);
        return ShoppingCartMapper.toDto(cartRepository.save(cart));
    }

    @Transactional
    public void deactivate(String username) {
        validateUsername(username);
        cartRepository.findFirstByUsernameAndActiveTrue(username).ifPresent(cart -> {
            cart.setActive(false);
            cartRepository.save(cart);
        });
    }

    @Transactional
    public ShoppingCartDto removeProducts(String username, List<UUID> productIds) {
        validateUsername(username);
        ShoppingCart cart = getOrCreateActiveCart(username);
        boolean removed = productIds.stream()
                .map(cart.getProducts()::remove)
                .anyMatch(quantity -> quantity != null);
        if (!removed) {
            throw new NoProductsInShoppingCartException();
        }
        return ShoppingCartMapper.toDto(cartRepository.save(cart));
    }

    @Transactional
    public ShoppingCartDto changeQuantity(String username, ChangeProductQuantityRequest request) {
        validateUsername(username);
        ShoppingCart cart = getOrCreateActiveCart(username);
        if (!cart.getProducts().containsKey(request.productId())) {
            throw new NoProductsInShoppingCartException();
        }
        cart.getProducts().put(request.productId(), request.newQuantity());
        verifyWarehouseAvailability(cart);
        return ShoppingCartMapper.toDto(cartRepository.save(cart));
    }

    private ShoppingCart getOrCreateActiveCart(String username) {
        return cartRepository.findFirstByUsernameAndActiveTrue(username).orElseGet(() -> {
            ShoppingCart cart = new ShoppingCart();
            cart.setShoppingCartId(UUID.randomUUID());
            cart.setUsername(username);
            cart.setActive(true);
            cart.setProducts(new HashMap<>());
            return cartRepository.save(cart);
        });
    }

    // Before saving cart changes, ask warehouse if all selected products are available.
    private void verifyWarehouseAvailability(ShoppingCart cart) {
        try {
            warehouseClient.checkProductQuantityEnoughForShoppingCart(ShoppingCartMapper.toDto(cart));
        } catch (FeignException.ServiceUnavailable ex) {
            throw new WarehouseUnavailableException(ex);
        } catch (NoFallbackAvailableException ex) {
            throw new WarehouseUnavailableException(ex);
        }
    }

    private void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new NotAuthorizedUserException();
        }
    }
}
