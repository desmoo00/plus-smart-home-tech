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
        if (productRepository.existsById(request.productId())) {
            throw new SpecifiedProductAlreadyInWarehouseException(request.productId());
        }
        WarehouseProduct product = new WarehouseProduct();
        DimensionDto dimension = request.dimension();
        product.setProductId(request.productId());
        product.setFragile(request.fragile());
        product.setWidth(dimension.width());
        product.setHeight(dimension.height());
        product.setDepth(dimension.depth());
        product.setWeight(request.weight());
        product.setQuantity(0);
        productRepository.save(product);
    }

    @Transactional
    public void addProduct(AddProductToWarehouseRequest request) {
        WarehouseProduct product = productRepository.findById(request.productId())
                .orElseThrow(() -> new NoSpecifiedProductInWarehouseException(request.productId()));
        product.setQuantity(product.getQuantity() + request.quantity());
        productRepository.save(product);
    }

    @Transactional
    // First check every product. If something is missing, we do not reserve any item.
    public BookedProductsDto checkAndBook(ShoppingCartDto cart) {
        Map<UUID, Long> missingProducts = new HashMap<>();
        double deliveryWeight = 0;
        double deliveryVolume = 0;
        boolean fragile = false;
        Map<WarehouseProduct, Long> productsToBook = new HashMap<>();

        for (Map.Entry<UUID, Long> entry : cart.products().entrySet()) {
            WarehouseProduct product = productRepository.findById(entry.getKey()).orElse(null);
            long requested = entry.getValue();
            if (product == null || product.getQuantity() < requested) {
                long available = product == null ? 0 : product.getQuantity();
                missingProducts.put(entry.getKey(), requested - available);
                continue;
            }
            deliveryWeight += product.getWeight() * requested;
            deliveryVolume += product.getWidth() * product.getHeight() * product.getDepth() * requested;
            fragile = fragile || product.isFragile();
            productsToBook.put(product, requested);
        }

        if (!missingProducts.isEmpty()) {
            throw new ProductInShoppingCartLowQuantityInWarehouseException(missingProducts);
        }

        productsToBook.forEach((product, requested) -> product.setQuantity(product.getQuantity() - requested));
        productRepository.saveAll(productsToBook.keySet());
        return new BookedProductsDto(deliveryWeight, deliveryVolume, fragile);
    }

    // The task asks to put the same generated value into every address field.
    public AddressDto getAddress() {
        return new AddressDto(CURRENT_ADDRESS, CURRENT_ADDRESS, CURRENT_ADDRESS, CURRENT_ADDRESS, CURRENT_ADDRESS);
    }
}
