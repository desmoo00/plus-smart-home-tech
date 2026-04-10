package ru.yandex.practicum.commerce.warehouse.service;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.api.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.api.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.commerce.api.warehouse.AddressDto;
import ru.yandex.practicum.commerce.api.warehouse.BookedProductsDto;
import ru.yandex.practicum.commerce.api.warehouse.DimensionDto;
import ru.yandex.practicum.commerce.api.warehouse.NewProductInWarehouseRequest;
import ru.yandex.practicum.commerce.warehouse.exception.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.commerce.warehouse.exception.ProductInShoppingCartLowQuantityInWarehouseException;
import ru.yandex.practicum.commerce.warehouse.exception.SpecifiedProductAlreadyInWarehouseException;
import ru.yandex.practicum.commerce.warehouse.model.WarehouseProduct;
import ru.yandex.practicum.commerce.warehouse.repository.WarehouseProductRepository;

@Service
public class WarehouseService {

    private static final String[] ADDRESSES = new String[]{"ADDRESS_1", "ADDRESS_2"};
    private static final String CURRENT_ADDRESS =
            ADDRESSES[Random.from(new SecureRandom()).nextInt(0, ADDRESSES.length)];

    private final WarehouseProductRepository productRepository;

    public WarehouseService(WarehouseProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public void registerProduct(NewProductInWarehouseRequest request) {
        checkProductIsNew(request.productId());
        productRepository.save(createWarehouseProduct(request));
    }

    @Transactional
    public void addProduct(AddProductToWarehouseRequest request) {
        WarehouseProduct product = findProduct(request.productId());
        addQuantity(product, request.quantity());
        productRepository.save(product);
    }

    @Transactional
    // Проверка товаров на наличие, иначе резервируем
    public BookedProductsDto checkAndBook(ShoppingCartDto cart) {
        Map<UUID, Long> missingProducts = new HashMap<>();
        double deliveryWeight = 0;
        double deliveryVolume = 0;
        boolean fragile = false;

        for (Map.Entry<UUID, Long> entry : cart.products().entrySet()) {
            WarehouseProduct product = findProductOrNull(entry.getKey());
            long requested = entry.getValue();
            if (hasNotEnoughQuantity(product, requested)) {
                addMissingProduct(missingProducts, entry.getKey(), product, requested);
                continue;
            }
            deliveryWeight += calculateDeliveryWeight(product, requested);
            deliveryVolume += calculateDeliveryVolume(product, requested);
            fragile = fragile || product.isFragile();
        }

        if (!missingProducts.isEmpty()) {
            throw new ProductInShoppingCartLowQuantityInWarehouseException(missingProducts);
        }

        return new BookedProductsDto(deliveryWeight, deliveryVolume, fragile);
    }

    public AddressDto getAddress() {
        return new AddressDto(CURRENT_ADDRESS, CURRENT_ADDRESS, CURRENT_ADDRESS, CURRENT_ADDRESS, CURRENT_ADDRESS);
    }

    private void checkProductIsNew(UUID productId) {
        if (productRepository.existsById(productId)) {
            throw new SpecifiedProductAlreadyInWarehouseException(productId);
        }
    }

    private WarehouseProduct createWarehouseProduct(NewProductInWarehouseRequest request) {
        WarehouseProduct product = new WarehouseProduct();
        DimensionDto dimension = request.dimension();
        product.setProductId(request.productId());
        product.setFragile(request.fragile());
        product.setWidth(dimension.width());
        product.setHeight(dimension.height());
        product.setDepth(dimension.depth());
        product.setWeight(request.weight());
        product.setQuantity(0);
        return product;
    }

    private WarehouseProduct findProduct(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new NoSpecifiedProductInWarehouseException(productId));
    }

    private WarehouseProduct findProductOrNull(UUID productId) {
        return productRepository.findById(productId).orElse(null);
    }

    private void addQuantity(WarehouseProduct product, long quantity) {
        product.setQuantity(product.getQuantity() + quantity);
    }

    private boolean hasNotEnoughQuantity(WarehouseProduct product, long requested) {
        return product == null || product.getQuantity() < requested;
    }

    private void addMissingProduct(
            Map<UUID, Long> missingProducts,
            UUID productId,
            WarehouseProduct product,
            long requested
    ) {
        long available = product == null ? 0 : product.getQuantity();
        missingProducts.put(productId, requested - available);
    }

    private double calculateDeliveryWeight(WarehouseProduct product, long requested) {
        return product.getWeight() * requested;
    }

    private double calculateDeliveryVolume(WarehouseProduct product, long requested) {
        return product.getWidth() * product.getHeight() * product.getDepth() * requested;
    }
}
