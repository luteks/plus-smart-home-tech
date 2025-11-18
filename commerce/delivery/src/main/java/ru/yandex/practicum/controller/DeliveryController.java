package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.DeliveryDto;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.service.DeliveryService;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/delivery")
@RequiredArgsConstructor
public class DeliveryController {
    private final DeliveryService deliveryService;

    @PutMapping
    public DeliveryDto planDelivery(@RequestBody @Valid DeliveryDto deliveryDto) {
        return deliveryService.planDelivery(deliveryDto);
    }

    @PostMapping("/successful")
    public void deliverySuccess(@RequestBody UUID deliveryId) {
        deliveryService.deliverySuccess(deliveryId);
    }

    @PostMapping("/picked")
    public void deliveryPicked(@RequestBody UUID deliveryId) {
        deliveryService.deliveryPicked(deliveryId);
    }

    @PostMapping("/failed")
    public void failedDelivery(@RequestBody UUID deliveryId) {
        deliveryService.failedDelivery(deliveryId);
    }

    @PostMapping("/cost")
    public Double deliveryCost(@RequestBody @Valid OrderDto orderDto) {
        return deliveryService.deliveryCost(orderDto);
    }
}