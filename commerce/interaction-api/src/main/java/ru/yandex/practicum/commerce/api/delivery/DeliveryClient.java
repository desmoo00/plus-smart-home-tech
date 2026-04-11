package ru.yandex.practicum.commerce.api.delivery;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.commerce.api.order.OrderDto;

@FeignClient(name = "delivery")
public interface DeliveryClient {

    @PutMapping("/api/v1/delivery")
    DeliveryDto planDelivery(@Valid @RequestBody DeliveryDto delivery);

    @PostMapping("/api/v1/delivery/successful")
    void deliverySuccessful(@RequestBody UUID orderId);

    @PostMapping("/api/v1/delivery/picked")
    void deliveryPicked(@RequestBody UUID orderId);

    @PostMapping("/api/v1/delivery/failed")
    void deliveryFailed(@RequestBody UUID orderId);

    @PostMapping("/api/v1/delivery/cost")
    BigDecimal deliveryCost(@RequestBody OrderDto order);
}
