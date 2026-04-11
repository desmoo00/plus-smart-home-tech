package ru.yandex.practicum.commerce.payment.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import ru.yandex.practicum.commerce.api.payment.PaymentState;

@Entity
@Table(name = "payments")
public class PaymentEntity {

    @Id
    private UUID paymentId;

    @Column(nullable = false)
    private UUID orderId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal productTotal;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal deliveryTotal;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal feeTotal;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalPayment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentState state;

    public UUID getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(UUID paymentId) {
        this.paymentId = paymentId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getProductTotal() {
        return productTotal;
    }

    public void setProductTotal(BigDecimal productTotal) {
        this.productTotal = productTotal;
    }

    public BigDecimal getDeliveryTotal() {
        return deliveryTotal;
    }

    public void setDeliveryTotal(BigDecimal deliveryTotal) {
        this.deliveryTotal = deliveryTotal;
    }

    public BigDecimal getFeeTotal() {
        return feeTotal;
    }

    public void setFeeTotal(BigDecimal feeTotal) {
        this.feeTotal = feeTotal;
    }

    public BigDecimal getTotalPayment() {
        return totalPayment;
    }

    public void setTotalPayment(BigDecimal totalPayment) {
        this.totalPayment = totalPayment;
    }

    public PaymentState getState() {
        return state;
    }

    public void setState(PaymentState state) {
        this.state = state;
    }
}
