package ru.yandex.practicum.commerce.order.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.commerce.order.model.OrderEntity;

public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {

    @EntityGraph(attributePaths = "products")
    List<OrderEntity> findAllByUsernameOrderByCreatedAtDesc(String username);

    @EntityGraph(attributePaths = "products")
    List<OrderEntity> findAllByOrderByCreatedAtDesc();

    @Override
    @EntityGraph(attributePaths = "products")
    Optional<OrderEntity> findById(UUID orderId);
}
