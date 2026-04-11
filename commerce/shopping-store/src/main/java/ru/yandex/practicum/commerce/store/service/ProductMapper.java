package ru.yandex.practicum.commerce.store.service;

import ru.yandex.practicum.commerce.api.store.ProductDto;
import ru.yandex.practicum.commerce.store.model.Product;

public final class ProductMapper {

    private ProductMapper() {
    }

    // Собираем DTO из сущности товара для ответа наружу.
    public static ProductDto toDto(Product product) {
        return new ProductDto(
                product.getProductId(),
                product.getProductName(),
                product.getDescription(),
                product.getImageSrc(),
                product.getQuantityState(),
                product.getProductState(),
                product.getProductCategory(),
                product.getPrice()
        );
    }

    // Создаем новую сущность товара и сразу заполняем ее данными из DTO.
    public static Product toEntity(ProductDto dto) {
        Product product = new Product();
        updateEntity(product, dto);
        return product;
    }

    // Переносим значения из DTO в сущность, чтобы использовать это и при создании, и при обновлении.
    public static void updateEntity(Product product, ProductDto dto) {
        product.setProductId(dto.productId());
        product.setProductName(dto.productName());
        product.setDescription(dto.description());
        product.setImageSrc(dto.imageSrc());
        product.setQuantityState(dto.quantityState());
        product.setProductState(dto.productState());
        product.setProductCategory(dto.productCategory());
        product.setPrice(dto.price());
    }
}
