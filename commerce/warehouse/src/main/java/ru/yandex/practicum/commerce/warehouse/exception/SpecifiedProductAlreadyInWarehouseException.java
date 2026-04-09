package ru.yandex.practicum.commerce.warehouse.exception;

import java.util.UUID;

public class SpecifiedProductAlreadyInWarehouseException extends RuntimeException {

    public SpecifiedProductAlreadyInWarehouseException(UUID productId) {
        super("Product is already registered in warehouse: " + productId);
    }
}
