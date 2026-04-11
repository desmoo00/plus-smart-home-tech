package ru.yandex.practicum.commerce.order.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import ru.yandex.practicum.commerce.api.order.OrderState;

@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    private UUID orderId;

    private UUID shoppingCartId;

    private String username;

    private UUID paymentId;

    private UUID deliveryId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderState state;

    private Double deliveryWeight;

    private Double deliveryVolume;

    private Boolean fragile;

    @Column(precision = 19, scale = 2)
    private BigDecimal totalPrice;

    @Column(precision = 19, scale = 2)
    private BigDecimal deliveryPrice;

    @Column(precision = 19, scale = 2)
    private BigDecimal productPrice;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ElementCollection
    @CollectionTable(name = "order_products", joinColumns = @JoinColumn(name = "order_id"))
    @MapKeyColumn(name = "product_id")
    @Column(name = "quantity", nullable = false)
    private Map<UUID, Long> products = new HashMap<>();

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "country", column = @Column(name = "delivery_country")),
            @AttributeOverride(name = "city", column = @Column(name = "delivery_city")),
            @AttributeOverride(name = "street", column = @Column(name = "delivery_street")),
            @AttributeOverride(name = "house", column = @Column(name = "delivery_house")),
            @AttributeOverride(name = "flat", column = @Column(name = "delivery_flat"))
    })
    private OrderAddress deliveryAddress;

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public UUID getShoppingCartId() {
        return shoppingCartId;
    }

    public void setShoppingCartId(UUID shoppingCartId) {
        this.shoppingCartId = shoppingCartId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(UUID paymentId) {
        this.paymentId = paymentId;
    }

    public UUID getDeliveryId() {
        return deliveryId;
    }

    public void setDeliveryId(UUID deliveryId) {
        this.deliveryId = deliveryId;
    }

    public OrderState getState() {
        return state;
    }

    public void setState(OrderState state) {
        this.state = state;
    }

    public Double getDeliveryWeight() {
        return deliveryWeight;
    }

    public void setDeliveryWeight(Double deliveryWeight) {
        this.deliveryWeight = deliveryWeight;
    }

    public Double getDeliveryVolume() {
        return deliveryVolume;
    }

    public void setDeliveryVolume(Double deliveryVolume) {
        this.deliveryVolume = deliveryVolume;
    }

    public Boolean getFragile() {
        return fragile;
    }

    public void setFragile(Boolean fragile) {
        this.fragile = fragile;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public BigDecimal getDeliveryPrice() {
        return deliveryPrice;
    }

    public void setDeliveryPrice(BigDecimal deliveryPrice) {
        this.deliveryPrice = deliveryPrice;
    }

    public BigDecimal getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(BigDecimal productPrice) {
        this.productPrice = productPrice;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Map<UUID, Long> getProducts() {
        return products;
    }

    public void setProducts(Map<UUID, Long> products) {
        this.products = products;
    }

    public OrderAddress getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(OrderAddress deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }
}
