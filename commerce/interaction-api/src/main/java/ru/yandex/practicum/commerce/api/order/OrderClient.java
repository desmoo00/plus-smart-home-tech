package ru.yandex.practicum.commerce.api.order;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "order")
public interface OrderClient {

    @GetMapping("/api/v1/order")
    List<OrderDto> getClientOrders(@RequestParam String username);

    @PutMapping("/api/v1/order")
    OrderDto createNewOrder(@Valid @RequestBody CreateNewOrderRequest request);

    @PostMapping("/api/v1/order/return")
    OrderDto productReturn(@Valid @RequestBody ProductReturnRequest request);

    @PostMapping("/api/v1/order/payment")
    OrderDto payment(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/payment/failed")
    OrderDto paymentFailed(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/delivery")
    OrderDto delivery(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/delivery/failed")
    OrderDto deliveryFailed(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/completed")
    OrderDto complete(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/calculate/total")
    OrderDto calculateTotalCost(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/calculate/delivery")
    OrderDto calculateDeliveryCost(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/assembly")
    OrderDto assembly(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/assembly/failed")
    OrderDto assemblyFailed(@RequestBody UUID orderId);
}
