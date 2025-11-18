package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.request.CreateNewOrderRequest;
import ru.yandex.practicum.request.ProductReturnRequest;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    OrderDto createOrder(CreateNewOrderRequest newOrderRequest);

    OrderDto returnOrder(ProductReturnRequest returnRequest);

    OrderDto payment(UUID orderId);

    OrderDto failedPayment(UUID orderId);

    OrderDto delivery(UUID orderId);

    OrderDto failedDelivery(UUID orderId);

    OrderDto completeOrder(UUID orderId);

    OrderDto calculateTotal(UUID orderId);

    OrderDto calculateDelivery(UUID orderId);

    OrderDto assembly(UUID orderId);

    OrderDto failedAssembly(UUID orderId);

    List<OrderDto> getOrders(String username, Integer page, Integer size);
}
