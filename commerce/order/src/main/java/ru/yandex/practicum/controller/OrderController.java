package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.request.CreateNewOrderRequest;
import ru.yandex.practicum.request.ProductReturnRequest;
import ru.yandex.practicum.service.OrderService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/order")
@AllArgsConstructor
@Validated
public class OrderController {
    private final OrderService orderService;

    @PutMapping
    public OrderDto createOrder(@RequestBody @Valid CreateNewOrderRequest newOrderRequest) {
        return orderService.createOrder(newOrderRequest);
    }

    @PostMapping("/return")
    public OrderDto returnOrder(@RequestBody @Valid ProductReturnRequest returnRequest) {
        return orderService.returnOrder(returnRequest);
    }

    @PostMapping("/payment")
    public OrderDto payment(@RequestBody UUID orderId) {
        return orderService.payment(orderId);
    }

    @PostMapping("/payment/failed")
    public OrderDto failedPayment(@RequestBody UUID orderId) {
        return orderService.failedPayment(orderId);
    }

    @PostMapping("/delivery")
    public OrderDto delivery(@RequestBody UUID orderId) {
        return orderService.delivery(orderId);
    }

    @PostMapping("/delivery/failed")
    public OrderDto failedDelivery(@RequestBody UUID orderId) {
        return orderService.failedDelivery(orderId);
    }

    @PostMapping("/completed")
    public OrderDto completeOrder(@RequestBody UUID orderId) {
        return orderService.completeOrder(orderId);
    }

    @PostMapping("/calculate/total")
    public OrderDto calculateTotal(@RequestBody UUID orderId) {
        return orderService.calculateTotal(orderId);
    }

    @PostMapping("/calculate/delivery")
    public OrderDto calculateDelivery(@RequestBody UUID orderId) {
        return orderService.calculateDelivery(orderId);
    }

    @PostMapping("/assembly")
    public OrderDto assembly(@RequestBody UUID orderId) {
        return orderService.assembly(orderId);
    }

    @PostMapping("/assembly/failed")
    public OrderDto failedAssembly(@RequestBody UUID orderId) {
        return orderService.failedAssembly(orderId);
    }

    @GetMapping
    public List<OrderDto> getOrders(@RequestParam String username,
                                    @RequestParam(defaultValue = "0") Integer page,
                                    @RequestParam(defaultValue = "10") Integer size) {
        return orderService.getOrders(username, page, size);
    }

}