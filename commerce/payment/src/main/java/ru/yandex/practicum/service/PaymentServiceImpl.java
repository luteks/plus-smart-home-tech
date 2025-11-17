package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.PaymentDto;
import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.enums.PaymentState;
import ru.yandex.practicum.exception.NotEnoughInfoInOrderToCalculateException;
import ru.yandex.practicum.exception.PaymentNotFoundException;
import ru.yandex.practicum.feign.OrderClient;
import ru.yandex.practicum.feign.ShoppingStoreClient;
import ru.yandex.practicum.mapper.PaymentMapper;
import ru.yandex.practicum.model.Payment;
import ru.yandex.practicum.repository.PaymentRepository;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final ShoppingStoreClient shoppingStoreClient;
    private final OrderClient orderClient;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    @Override
    public PaymentDto createPayment(OrderDto orderDto) {
        checkOrder(orderDto);
        Payment payment = Payment.builder()
                .orderId(orderDto.getOrderId())
                .totalPayment(orderDto.getTotalPrice())
                .deliveryTotal(orderDto.getDeliveryPrice())
                .productsTotal(orderDto.getProductPrice())
                .feeTotal(getTax(orderDto.getTotalPrice()))
                .status(PaymentState.PENDING)
                .build();

        PaymentDto paymentDto = paymentMapper.toPaymentDto(paymentRepository.save(payment));
        log.debug("Платеж создан. {}", paymentDto);
        return paymentDto;
    }

    @Transactional(readOnly = true)
    @Override
    public Double getTotalCost(OrderDto orderDto) {
        if (orderDto.getDeliveryPrice() == null) {
            throw new NotEnoughInfoInOrderToCalculateException("В заказе отсутствует информация для расчёта.");
        }

        double totalCost = orderDto.getProductPrice() + getTax(orderDto.getProductPrice()) + orderDto.getDeliveryPrice();

        log.debug("Рассчитана полная стоимость заказа {}.", totalCost);
        return totalCost;
    }

    @Override
    public void paymentSuccess(UUID uuid) {
        Payment payment = getPayment(uuid);
        payment.setStatus(PaymentState.SUCCESS);
        orderClient.payment(payment.getOrderId());

        log.debug("Платеж id = {} совершен успешно.", payment.getPaymentId());
    }

    @Transactional(readOnly = true)
    @Override
    public Double productCost(OrderDto orderDto) {
        double productCost = 0.0;
        Map<UUID, Long> products = orderDto.getProducts();

        if (products == null) {
            log.error("В заказе id = '{}' отсутствует информация о продуктах для расчёта.", orderDto.getOrderId());
            throw new NotEnoughInfoInOrderToCalculateException("В заказе id = '"
                    + orderDto.getOrderId() + "' отсутствует информация о продуктах для расчёта.");
        }

        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            ProductDto product = shoppingStoreClient.getProduct(entry.getKey());
            productCost += product.getPrice() * entry.getValue();
        }

        log.debug("Стоимость товаров вычислена {}.", productCost);
        return productCost;
    }

    @Override
    public void paymentFailed(UUID uuid) {
        Payment payment = getPayment(uuid);
        payment.setStatus(PaymentState.FAILED);
        orderClient.failedPayment(payment.getOrderId());

        log.debug("Платеж id = '{}' не удался", payment.getPaymentId());
    }

    private Payment getPayment(UUID uuid) {
        return paymentRepository.findById(uuid)
                .orElseThrow(() -> {
                    log.error("Оплата заказа с id: {} не найдена.", uuid);
                    return new PaymentNotFoundException("Оплата заказа с id: " + uuid + " не найдена.");
                });
    }

    private void checkOrder(OrderDto orderDto) {
        if (orderDto.getDeliveryPrice() == null) {
            log.error("В заказе id = '{}' отсутствует информация о стоимости доставки для расчёта.",
                    orderDto.getOrderId());
            throw new NotEnoughInfoInOrderToCalculateException("В заказе " + orderDto.getOrderId()
                    + " отсутствует информация о стоимости доставки для расчёта.");
        }
        if (orderDto.getProductPrice() == null) {
            log.error("В заказе id = '{}' отсутствует информация о стоимости товара для расчёта.",
                    orderDto.getOrderId());
            throw new NotEnoughInfoInOrderToCalculateException("В заказе " + orderDto.getOrderId()
                    + " отсутствует информация о стоимости товара для расчёта.");
        }
        if (orderDto.getTotalPrice() == null) {
            log.error("В заказе id = '{}' отсутствует информация о полной стоимости заказа для расчёта.",
                    orderDto.getOrderId());
            throw new NotEnoughInfoInOrderToCalculateException("В заказе " + orderDto.getOrderId()
                    + " отсутствует информация о полной стоимости заказа для расчёта.");
        }
    }

    private double getTax(double totalPrice) {
        double feeRatio = 0.1;
        return totalPrice * feeRatio;
    }

}