package ru.yandex.practicum.commerce.warehouse.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.commerce.warehouse.model.WarehouseProduct;

public interface WarehouseProductRepository extends JpaRepository<WarehouseProduct, UUID> {
}
