package ru.yandex.practicum.commerce.delivery.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.api.delivery.DeliveryDto;
import ru.yandex.practicum.commerce.api.delivery.DeliveryState;
import ru.yandex.practicum.commerce.api.order.OrderClient;
import ru.yandex.practicum.commerce.api.order.OrderDto;
import ru.yandex.practicum.commerce.api.warehouse.ShippedToDeliveryRequest;
import ru.yandex.practicum.commerce.api.warehouse.WarehouseClient;
import ru.yandex.practicum.commerce.delivery.exception.NoDeliveryFoundException;
import ru.yandex.practicum.commerce.delivery.model.DeliveryEntity;
import ru.yandex.practicum.commerce.delivery.repository.DeliveryRepository;

@Service
public class DeliveryService {

    private static final Logger log = LoggerFactory.getLogger(DeliveryService.class);

    private static final BigDecimal BASE_RATE = BigDecimal.valueOf(5.0);
    private static final BigDecimal FRAGILE_RATE = BigDecimal.valueOf(0.2);
    private static final BigDecimal WEIGHT_RATE = BigDecimal.valueOf(0.3);
    private static final BigDecimal VOLUME_RATE = BigDecimal.valueOf(0.2);
    private static final BigDecimal STREET_RATE = BigDecimal.valueOf(0.2);
    private static final String WAREHOUSE_ADDRESS_2 = "ADDRESS_2";

    private final DeliveryRepository deliveryRepository;
    private final OrderClient orderClient;
    private final WarehouseClient warehouseClient;

    public DeliveryService(
            DeliveryRepository deliveryRepository,
            OrderClient orderClient,
            WarehouseClient warehouseClient
    ) {
        this.deliveryRepository = deliveryRepository;
        this.orderClient = orderClient;
        this.warehouseClient = warehouseClient;
    }

    @Transactional
    public DeliveryDto planDelivery(DeliveryDto delivery) {
        DeliveryEntity entity = DeliveryMapper.toEntity(delivery);
        if (entity.getDeliveryId() == null) {
            entity.setDeliveryId(UUID.randomUUID());
        }
        entity.setDeliveryState(DeliveryState.CREATED);
        return DeliveryMapper.toDto(deliveryRepository.save(entity));
    }

    @Transactional
    public void deliverySuccessful(UUID orderId) {
        DeliveryEntity delivery = getByOrderId(orderId);
        delivery.setDeliveryState(DeliveryState.DELIVERED);
        deliveryRepository.save(delivery);
        orderClient.delivery(orderId);
    }

    @Transactional
    public void deliveryPicked(UUID orderId) {
        DeliveryEntity delivery = getByOrderId(orderId);
        delivery.setDeliveryState(DeliveryState.IN_PROGRESS);
        deliveryRepository.save(delivery);
        orderClient.assembly(orderId);
        warehouseClient.shippedToDelivery(new ShippedToDeliveryRequest(orderId, delivery.getDeliveryId()));
    }

    @Transactional
    public void deliveryFailed(UUID orderId) {
        DeliveryEntity delivery = getByOrderId(orderId);
        delivery.setDeliveryState(DeliveryState.FAILED);
        deliveryRepository.save(delivery);
        orderClient.deliveryFailed(orderId);
    }

    @Transactional(readOnly = true)
    public BigDecimal deliveryCost(OrderDto order) {
        DeliveryEntity delivery = getByOrder(order);
        int warehouseRate = getWarehouseRate(delivery);
        boolean sameStreet = isSameStreet(delivery);
        BigDecimal basePrice = calculateBaseDeliveryPrice(warehouseRate);
        BigDecimal fragileExtra = BigDecimal.ZERO;
        BigDecimal weightExtra = BigDecimal.valueOf(defaultDouble(order.deliveryWeight())).multiply(WEIGHT_RATE);
        BigDecimal volumeExtra = BigDecimal.valueOf(defaultDouble(order.deliveryVolume())).multiply(VOLUME_RATE);
        BigDecimal result = basePrice;

        log.info(
                "Start delivery cost calculation: orderId={}, deliveryId={}, fragile={}, deliveryWeight={}, " +
                        "deliveryVolume={}, fromStreet={}, toStreet={}",
                order.orderId(),
                delivery.getDeliveryId(),
                order.fragile(),
                order.deliveryWeight(),
                order.deliveryVolume(),
                safe(delivery.getFromAddress().getStreet()),
                safe(delivery.getToAddress().getStreet())
        );

        if (Boolean.TRUE.equals(order.fragile())) {
            fragileExtra = result.multiply(FRAGILE_RATE);
            result = result.add(fragileExtra);
        }

        result = result.add(weightExtra);
        result = result.add(volumeExtra);

        BigDecimal streetExtra = BigDecimal.ZERO;
        if (!sameStreet) {
            streetExtra = result.multiply(STREET_RATE);
            result = result.add(streetExtra);
        }

        BigDecimal finalCost = result.setScale(2, RoundingMode.HALF_UP);

        log.info(
                "Finish delivery cost calculation: orderId={}, deliveryId={}, warehouseRate={}, sameStreet={}, " +
                        "basePrice={}, fragileExtra={}, weightExtra={}, volumeExtra={}, streetExtra={}, finalCost={}",
                order.orderId(),
                delivery.getDeliveryId(),
                warehouseRate,
                sameStreet,
                basePrice,
                fragileExtra,
                weightExtra,
                volumeExtra,
                streetExtra,
                finalCost
        );

        return finalCost;
    }

    // Вспомогательный метод: рассчитывает базовую стоимость доставки
    // на основе базового тарифа и коэффициента склада (адреса отправки).
    private BigDecimal calculateBaseDeliveryPrice(int warehouseRate) {
        BigDecimal warehousePart = BASE_RATE.multiply(BigDecimal.valueOf(warehouseRate));
        return BASE_RATE.add(warehousePart);
    }

    private DeliveryEntity getByOrder(OrderDto order) {
        if (order.deliveryId() != null) {
            return deliveryRepository.findById(order.deliveryId())
                    .orElseThrow(() -> new NoDeliveryFoundException(order.orderId()));
        }
        return getByOrderId(order.orderId());
    }

    private DeliveryEntity getByOrderId(UUID orderId) {
        return deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NoDeliveryFoundException(orderId));
    }

    // определяет коэффициент для склада в зависимости от адреса отправки.
    private int getWarehouseRate(DeliveryEntity delivery) {
        String source = buildAddressSource(delivery);
        if (source.contains(WAREHOUSE_ADDRESS_2)) {
            return 2;
        }
        return 1;
    }

    private boolean isSameStreet(DeliveryEntity delivery) {
        String fromStreet = safe(delivery.getFromAddress().getStreet());
        String toStreet = safe(delivery.getToAddress().getStreet());
        return fromStreet.equalsIgnoreCase(toStreet);
    }

    private String buildAddressSource(DeliveryEntity delivery) {
        return String.join(" ",
                safe(delivery.getFromAddress().getCountry()),
                safe(delivery.getFromAddress().getCity()),
                safe(delivery.getFromAddress().getStreet()),
                safe(delivery.getFromAddress().getHouse()),
                safe(delivery.getFromAddress().getFlat())
        );
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private double defaultDouble(Double value) {
        return value == null ? 0.0 : value;
    }
}
