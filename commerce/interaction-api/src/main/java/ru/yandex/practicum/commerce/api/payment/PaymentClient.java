package ru.yandex.practicum.commerce.api.payment;

import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.commerce.api.order.OrderDto;

@FeignClient(name = "payment")
public interface PaymentClient {

    @PostMapping("/api/v1/payment")
    PaymentDto payment(@RequestBody OrderDto order);

    @PostMapping("/api/v1/payment/totalCost")
    BigDecimal getTotalCost(@RequestBody OrderDto order);

    @PostMapping("/api/v1/payment/refund")
    void paymentSuccess(@RequestBody UUID paymentId);

    @PostMapping("/api/v1/payment/productCost")
    BigDecimal productCost(@RequestBody OrderDto order);

    @PostMapping("/api/v1/payment/failed")
    void paymentFailed(@RequestBody UUID paymentId);
}
