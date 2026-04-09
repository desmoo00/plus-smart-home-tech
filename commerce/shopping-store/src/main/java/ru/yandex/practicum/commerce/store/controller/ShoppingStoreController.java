package ru.yandex.practicum.commerce.store.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.api.store.ProductCategory;
import ru.yandex.practicum.commerce.api.store.ProductDto;
import ru.yandex.practicum.commerce.api.store.SetProductQuantityStateRequest;
import ru.yandex.practicum.commerce.api.store.ShoppingStoreClient;
import ru.yandex.practicum.commerce.store.service.ProductService;

@RestController
public class ShoppingStoreController implements ShoppingStoreClient {

    private final ProductService productService;

    public ShoppingStoreController(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public Page<ProductDto> getProducts(ProductCategory category, int page, int size, List<String> sort) {
        return productService.getProducts(category, page, size, sort);
    }

    @Override
    public ProductDto createNewProduct(@Valid ProductDto product) {
        return productService.create(product);
    }

    @Override
    public ProductDto updateProduct(@Valid ProductDto product) {
        return productService.update(product);
    }

    @Override
    public Boolean removeProductFromStore(UUID productId) {
        return productService.remove(productId);
    }

    @Override
    public Boolean setProductQuantityState(@Valid SetProductQuantityStateRequest request) {
        return productService.setQuantityState(request);
    }

    @Override
    public ProductDto getProduct(UUID productId) {
        return productService.getProduct(productId);
    }
}
