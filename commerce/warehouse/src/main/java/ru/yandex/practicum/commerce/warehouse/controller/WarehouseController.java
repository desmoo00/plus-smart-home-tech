package ru.yandex.practicum.commerce.warehouse.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.api.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.api.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.commerce.api.warehouse.AddressDto;
import ru.yandex.practicum.commerce.api.warehouse.BookedProductsDto;
import ru.yandex.practicum.commerce.api.warehouse.NewProductInWarehouseRequest;
import ru.yandex.practicum.commerce.api.warehouse.WarehouseClient;
import ru.yandex.practicum.commerce.warehouse.service.WarehouseService;

@RestController
public class WarehouseController implements WarehouseClient {

    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @Override
    public void newProductInWarehouse(@Valid NewProductInWarehouseRequest request) {
        warehouseService.registerProduct(request);
    }

    @Override
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(@Valid ShoppingCartDto cart) {
        return warehouseService.checkAndBook(cart);
    }

    @Override
    public void addProductToWarehouse(@Valid AddProductToWarehouseRequest request) {
        warehouseService.addProduct(request);
    }

    @Override
    public AddressDto getWarehouseAddress() {
        return warehouseService.getAddress();
    }
}
