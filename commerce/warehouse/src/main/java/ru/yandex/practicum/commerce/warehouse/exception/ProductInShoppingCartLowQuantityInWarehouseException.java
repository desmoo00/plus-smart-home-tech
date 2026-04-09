package ru.yandex.practicum.commerce.warehouse.exception;

import java.util.Map;
import java.util.UUID;

public class ProductInShoppingCartLowQuantityInWarehouseException extends RuntimeException {

    public ProductInShoppingCartLowQuantityInWarehouseException(Map<UUID, Long> missingProducts) {
        super("Not enough products in warehouse: " + missingProducts);
    }
}
