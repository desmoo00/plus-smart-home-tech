package ru.yandex.practicum.commerce.store.service;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.api.store.ProductCategory;
import ru.yandex.practicum.commerce.api.store.ProductDto;
import ru.yandex.practicum.commerce.api.store.ProductState;
import ru.yandex.practicum.commerce.api.store.QuantityState;
import ru.yandex.practicum.commerce.store.exception.ProductNotFoundException;
import ru.yandex.practicum.commerce.store.model.Product;
import ru.yandex.practicum.commerce.store.repository.ProductRepository;

@Service
public class ProductService {

    private static final String DEFAULT_SORT_PROPERTY = "productName";

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> getProducts(ProductCategory category, int page, int size, List<String> sort) {
        Pageable pageable = PageRequest.of(page, size, parseSort(sort));
        return productRepository.findByProductCategory(category, pageable)
                .map(ProductMapper::toDto);
    }

    @Transactional
    public ProductDto create(ProductDto dto) {
        Product product = ProductMapper.toEntity(dto);
        product.setProductId(getProductIdForCreate(dto));
        return ProductMapper.toDto(productRepository.save(product));
    }

    @Transactional
    public ProductDto update(ProductDto dto) {
        Product product = findProduct(dto.productId());
        ProductMapper.updateEntity(product, dto);
        return ProductMapper.toDto(productRepository.save(product));
    }

    @Transactional
    public boolean remove(UUID productId) {
        Product product = findProduct(productId);
        product.setProductState(ProductState.DEACTIVATE);
        productRepository.save(product);
        return true;
    }

    @Transactional
    public boolean setQuantityState(UUID productId, QuantityState quantityState) {
        Product product = findProduct(productId);
        product.setQuantityState(quantityState);
        productRepository.save(product);
        return true;
    }

    @Transactional(readOnly = true)
    public ProductDto getProduct(UUID productId) {
        return ProductMapper.toDto(findProduct(productId));
    }

    // Клиент отправляет сортировку в виде списка типа ["цена", "описание"], поэтому мы преобразуем ее в Spring сортировку
    private Sort parseSort(List<String> sort) {
        if (sort == null || sort.isEmpty()) {
            return Sort.by(DEFAULT_SORT_PROPERTY).ascending();
        }
        return Sort.by(getSortDirection(sort), getSortProperty(sort));
    }

    private UUID getProductIdForCreate(ProductDto dto) {
        return dto.productId() == null ? UUID.randomUUID() : dto.productId();
    }

    private Product findProduct(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    private String getSortProperty(List<String> sort) {
        return sort.get(0);
    }

    private Sort.Direction getSortDirection(List<String> sort) {
        if (sort.size() <= 1) {
            return Sort.Direction.ASC;
        }
        return Sort.Direction.fromOptionalString(sort.get(1)).orElse(Sort.Direction.ASC);
    }
}
