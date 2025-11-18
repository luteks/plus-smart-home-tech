package ru.yandex.practicum.feign;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.request.CreateNewOrderRequest;
import ru.yandex.practicum.request.ProductReturnRequest;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "order", path = "/api/v1/order")
public interface OrderClient {
    @PutMapping
    OrderDto createOrder(@RequestBody @Valid CreateNewOrderRequest newOrderRequest);

    @PostMapping("/return")
    OrderDto returnOrder(@RequestBody @Valid ProductReturnRequest returnRequest);

    @PostMapping("/payment")
    OrderDto payment(@RequestBody UUID orderId);

    @PostMapping("/payment/failed")
    OrderDto failedPayment(@RequestBody UUID orderId);

    @PostMapping("/delivery")
    OrderDto delivery(@RequestBody UUID orderId);

    @PostMapping("/delivery/failed")
    OrderDto failedDelivery(@RequestBody UUID orderId);

    @PostMapping("/completed")
    OrderDto completeOrder(@RequestBody UUID orderId);

    @PostMapping("/calculate/total")
    OrderDto calculateTotal(@RequestBody UUID orderId);

    @PostMapping("/calculate/delivery")
    OrderDto calculateDelivery(@RequestBody UUID orderId);

    @PostMapping("/assembly")
    OrderDto assembly(@RequestBody UUID orderId);

    @PostMapping("/assembly/failed")
    OrderDto failedAssembly(@RequestBody UUID orderId);

    @GetMapping
    List<OrderDto> getOrders(@RequestParam String username, @RequestParam Integer page, @RequestParam Integer size);
}