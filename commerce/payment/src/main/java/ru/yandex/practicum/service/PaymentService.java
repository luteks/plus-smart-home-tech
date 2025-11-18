package ru.yandex.practicum.service;

import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.PaymentDto;

import java.util.UUID;

public interface PaymentService {
    PaymentDto createPayment(OrderDto orderDto);

    @Transactional(readOnly = true)
    Double getTotalCost(OrderDto orderDto);

    void paymentSuccess(UUID uuid);

    @Transactional(readOnly = true)
    Double productCost(OrderDto orderDto);

    void paymentFailed(UUID uuid);
}
