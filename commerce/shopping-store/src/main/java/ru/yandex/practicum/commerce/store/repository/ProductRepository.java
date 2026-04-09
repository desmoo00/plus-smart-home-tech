package ru.yandex.practicum.commerce.store.repository;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.commerce.api.store.ProductCategory;
import ru.yandex.practicum.commerce.api.store.ProductState;
import ru.yandex.practicum.commerce.store.model.Product;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    Page<Product> findByProductCategoryAndProductState(ProductCategory category, ProductState state, Pageable pageable);
}
