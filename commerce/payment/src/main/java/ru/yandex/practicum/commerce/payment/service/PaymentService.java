package ru.yandex.practicum.commerce.payment.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.api.order.OrderClient;
import ru.yandex.practicum.commerce.api.order.OrderDto;
import ru.yandex.practicum.commerce.api.payment.PaymentDto;
import ru.yandex.practicum.commerce.api.payment.PaymentState;
import ru.yandex.practicum.commerce.api.store.ProductDto;
import ru.yandex.practicum.commerce.api.store.ShoppingStoreClient;
import ru.yandex.practicum.commerce.payment.exception.NoPaymentFoundException;
import ru.yandex.practicum.commerce.payment.exception.NotEnoughInfoInOrderToCalculateException;
import ru.yandex.practicum.commerce.payment.model.PaymentEntity;
import ru.yandex.practicum.commerce.payment.repository.PaymentRepository;

@Service
public class PaymentService {

    private static final BigDecimal TAX_RATE = BigDecimal.valueOf(0.10);

    private final PaymentRepository paymentRepository;
    private final ShoppingStoreClient shoppingStoreClient;
    private final OrderClient orderClient;

    public PaymentService(
            PaymentRepository paymentRepository,
            ShoppingStoreClient shoppingStoreClient,
            OrderClient orderClient
    ) {
        this.paymentRepository = paymentRepository;
        this.shoppingStoreClient = shoppingStoreClient;
        this.orderClient = orderClient;
    }

    @Transactional
    public PaymentDto payment(OrderDto order) {
        PaymentSummary summary = buildPaymentSummary(order);
        PaymentDto dto = new PaymentDto(
                UUID.randomUUID(),
                summary.totalPayment(),
                summary.deliveryTotal(),
                summary.feeTotal()
        );
        PaymentEntity payment = PaymentMapper.create(order.orderId(), dto);
        paymentRepository.save(payment);
        return dto;
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalCost(OrderDto order) {
        return buildPaymentSummary(order).totalPayment();
    }

    @Transactional
    public void paymentSuccess(UUID paymentId) {
        PaymentEntity payment = getPayment(paymentId);
        payment.setState(PaymentState.SUCCESS);
        paymentRepository.save(payment);
        orderClient.payment(payment.getOrderId());
    }

    @Transactional(readOnly = true)
    public BigDecimal productCost(OrderDto order) {
        if (order.products() == null || order.products().isEmpty()) {
            throw new NotEnoughInfoInOrderToCalculateException("Order must contain products");
        }

        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<UUID, Long> entry : order.products().entrySet()) {
            ProductDto product = shoppingStoreClient.getProduct(entry.getKey());
            total = total.add(product.price().multiply(BigDecimal.valueOf(entry.getValue())));
        }
        return scaleMoney(total);
    }

    @Transactional
    public void paymentFailed(UUID paymentId) {
        PaymentEntity payment = getPayment(paymentId);
        payment.setState(PaymentState.FAILED);
        paymentRepository.save(payment);
        orderClient.paymentFailed(payment.getOrderId());
    }

    // Считает все части оплаты один раз, чтобы дальше использовать уже готовые значения
    private PaymentSummary buildPaymentSummary(OrderDto order) {
        BigDecimal productTotal = resolveProductTotal(order);
        BigDecimal deliveryTotal = requireValue(order.deliveryPrice(), "Delivery price is required");
        BigDecimal feeTotal = calculateFee(productTotal);
        BigDecimal totalPayment = scaleMoney(productTotal.add(feeTotal).add(deliveryTotal));
        return new PaymentSummary(productTotal, deliveryTotal, feeTotal, totalPayment);
    }

    private PaymentEntity getPayment(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NoPaymentFoundException(paymentId));
    }

    private BigDecimal resolveProductTotal(OrderDto order) {
        if (order.productPrice() != null) {
            return scaleMoney(order.productPrice());
        }
        return productCost(order);
    }

    private BigDecimal calculateFee(BigDecimal productTotal) {
        return scaleMoney(productTotal.multiply(TAX_RATE));
    }

    private BigDecimal requireValue(BigDecimal value, String message) {
        if (value == null) {
            throw new NotEnoughInfoInOrderToCalculateException(message);
        }
        return scaleMoney(value);
    }

    private BigDecimal scaleMoney(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private record PaymentSummary(
            BigDecimal productTotal,
            BigDecimal deliveryTotal,
            BigDecimal feeTotal,
            BigDecimal totalPayment
    ) {
    }
}
