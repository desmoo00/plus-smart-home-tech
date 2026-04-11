package ru.yandex.practicum.commerce.warehouse.service;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.api.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.api.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.commerce.api.warehouse.AddressDto;
import ru.yandex.practicum.commerce.api.warehouse.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.commerce.api.warehouse.BookedProductsDto;
import ru.yandex.practicum.commerce.api.warehouse.DimensionDto;
import ru.yandex.practicum.commerce.api.warehouse.NewProductInWarehouseRequest;
import ru.yandex.practicum.commerce.api.warehouse.ShippedToDeliveryRequest;
import ru.yandex.practicum.commerce.warehouse.exception.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.commerce.warehouse.exception.ProductInShoppingCartLowQuantityInWarehouseException;
import ru.yandex.practicum.commerce.warehouse.exception.SpecifiedProductAlreadyInWarehouseException;
import ru.yandex.practicum.commerce.warehouse.model.OrderBooking;
import ru.yandex.practicum.commerce.warehouse.model.WarehouseProduct;
import ru.yandex.practicum.commerce.warehouse.repository.OrderBookingRepository;
import ru.yandex.practicum.commerce.warehouse.repository.WarehouseProductRepository;

@Service
public class WarehouseService {

    private static final String[] ADDRESSES = {"ADDRESS_1", "ADDRESS_2"};
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String CURRENT_ADDRESS = ADDRESSES[RANDOM.nextInt(ADDRESSES.length)];

    private final WarehouseProductRepository productRepository;
    private final OrderBookingRepository orderBookingRepository;

    public WarehouseService(
            WarehouseProductRepository productRepository,
            OrderBookingRepository orderBookingRepository
    ) {
        this.productRepository = productRepository;
        this.orderBookingRepository = orderBookingRepository;
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

    // Проверка товаров на наличие, иначе резервируем
    @Transactional
    public BookedProductsDto assemblyProducts(AssemblyProductsForOrderRequest request) {
        BookingCalculationResult result = calculateBooking(request.products(), true);
        productRepository.saveAll(result.products().values());
        orderBookingRepository.save(createBooking(request));
        return result.bookedProducts();
    }

    @Transactional
    public void shippedToDelivery(ShippedToDeliveryRequest request) {
        OrderBooking booking = orderBookingRepository.findById(request.orderId())
                .orElseGet(() -> createEmptyBooking(request.orderId()));
        booking.setDeliveryId(request.deliveryId());
        orderBookingRepository.save(booking);
    }

    @Transactional
    public void acceptReturn(Map<UUID, Long> products) {
        Map<UUID, WarehouseProduct> storedProducts = loadProductsById(products);
        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            WarehouseProduct product = storedProducts.get(entry.getKey());
            if (product == null) {
                throw new NoSpecifiedProductInWarehouseException(entry.getKey());
            }
            addQuantity(product, entry.getValue());
        }
        productRepository.saveAll(storedProducts.values());
    }

    @Transactional
    public BookedProductsDto checkAndBook(ShoppingCartDto cart) {
        return calculateBooking(cart.products(), false).bookedProducts();
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

    // Сначала забираем все товары корзины одним запросом, чтобы не ходить в базу внутри цикла.
    private Map<UUID, WarehouseProduct> loadProductsById(Map<UUID, Long> productsToLoad) {
        Map<UUID, WarehouseProduct> products = new HashMap<>();
        for (WarehouseProduct product : productRepository.findAllById(productsToLoad.keySet())) {
            products.put(product.getProductId(), product);
        }
        return products;
    }

    // Общий расчёт для проверки корзины и сборки заказа:
    // проверяет остатки, считает параметры доставки и при необходимости списывает товары.
    private BookingCalculationResult calculateBooking(Map<UUID, Long> requestedProducts, boolean decreaseStock) {
        Map<UUID, Long> missingProducts = new HashMap<>();
        Map<UUID, WarehouseProduct> products = loadProductsById(requestedProducts);
        double deliveryWeight = 0;
        double deliveryVolume = 0;
        boolean fragile = false;

        for (Map.Entry<UUID, Long> entry : requestedProducts.entrySet()) {
            WarehouseProduct product = products.get(entry.getKey());
            long requested = entry.getValue();
            if (hasNotEnoughQuantity(product, requested)) {
                addMissingProduct(missingProducts, entry.getKey(), product, requested);
                continue;
            }

            deliveryWeight += calculateDeliveryWeight(product, requested);
            deliveryVolume += calculateDeliveryVolume(product, requested);
            fragile = fragile || product.isFragile();

            if (decreaseStock) {
                product.setQuantity(product.getQuantity() - requested);
            }
        }

        if (!missingProducts.isEmpty()) {
            throw new ProductInShoppingCartLowQuantityInWarehouseException(missingProducts);
        }

        return new BookingCalculationResult(
                products,
                new BookedProductsDto(deliveryWeight, deliveryVolume, fragile)
        );
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

    private OrderBooking createBooking(AssemblyProductsForOrderRequest request) {
        OrderBooking booking = new OrderBooking();
        booking.setOrderId(request.orderId());
        booking.setProducts(new HashMap<>(request.products()));
        return booking;
    }

    private OrderBooking createEmptyBooking(UUID orderId) {
        OrderBooking booking = new OrderBooking();
        booking.setOrderId(orderId);
        return booking;
    }

    private record BookingCalculationResult(
            Map<UUID, WarehouseProduct> products,
            BookedProductsDto bookedProducts
    ) {
    }
}
