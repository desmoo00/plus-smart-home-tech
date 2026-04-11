package ru.yandex.practicum.commerce.warehouse.exception;

import java.util.UUID;

public class NoSpecifiedProductInWarehouseException extends RuntimeException {

    public NoSpecifiedProductInWarehouseException(UUID productId) {
        super("Product is not registered in warehouse: " + productId);
    }
}
