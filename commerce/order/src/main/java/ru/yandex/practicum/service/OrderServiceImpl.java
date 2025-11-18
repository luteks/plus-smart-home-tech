package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.BookedProductsDto;
import ru.yandex.practicum.dto.DeliveryDto;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.enums.OrderState;
import ru.yandex.practicum.exception.NotAuthorizedUserException;
import ru.yandex.practicum.exception.OrderNotFoundException;
import ru.yandex.practicum.feign.DeliveryClient;
import ru.yandex.practicum.feign.PaymentClient;
import ru.yandex.practicum.feign.ShoppingCartClient;
import ru.yandex.practicum.feign.WarehouseClient;
import ru.yandex.practicum.mapper.OrderMapper;
import ru.yandex.practicum.model.Order;
import ru.yandex.practicum.repository.OrderRepository;
import ru.yandex.practicum.request.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.request.CreateNewOrderRequest;
import ru.yandex.practicum.request.ProductReturnRequest;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final ShoppingCartClient shoppingCartClient;
    private final WarehouseClient warehouseClient;
    private final PaymentClient paymentClient;
    private final DeliveryClient deliveryClient;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Override
    public OrderDto createOrder(CreateNewOrderRequest newOrderRequest) {
        Order order = buildOrder(newOrderRequest);
        Order newOrder = orderRepository.save(order);

        BookedProductsDto bookedProducts = warehouseClient.assemblyProductForOrder(
                new AssemblyProductsForOrderRequest(
                        newOrderRequest.getShoppingCartDto().getShoppingCartId(),
                        newOrder.getOrderId()
                ));

        newOrder.setFragile(bookedProducts.getFragile());
        newOrder.setDeliveryVolume(bookedProducts.getDeliveryVolume());
        newOrder.setDeliveryWeight(bookedProducts.getDeliveryWeight());
        newOrder.setProductPrice(paymentClient.productCost(orderMapper.toOrderDto(newOrder)));

        DeliveryDto deliveryDto = DeliveryDto.builder()
                .orderId(newOrder.getOrderId())
                .fromAddress(warehouseClient.getAddress())
                .toAddress(newOrderRequest.getAddressDto())
                .build();
        newOrder.setDeliveryId(deliveryClient.planDelivery(deliveryDto).getDeliveryId());

        paymentClient.createPayment(orderMapper.toOrderDto(newOrder));

        OrderDto orderDto = orderMapper.toOrderDto(newOrder);

        log.debug("Создан новый заказ. {}", orderDto);
        return orderDto;
    }

    private Order buildOrder(CreateNewOrderRequest newOrderRequest) {
        return Order.builder()
                .shoppingCartId(newOrderRequest.getShoppingCartDto().getShoppingCartId())
                .products(
                        newOrderRequest.getShoppingCartDto().getProducts()
                                .entrySet()
                                .stream()
                                .collect(Collectors.toMap(
                                        Map.Entry::getKey,
                                        Map.Entry::getValue
                                ))
                )
                .state(OrderState.NEW)
                .build();
    }

    @Override
    public OrderDto returnOrder(ProductReturnRequest returnRequest) {
        Order order = getOrder(returnRequest.getOrderId());
        warehouseClient.acceptReturn(returnRequest.getProducts());
        order.setState(OrderState.PRODUCT_RETURNED);

        log.debug("Заказ id = '{}' был возвращен.", order.getOrderId());
        return orderMapper.toOrderDto(order);
    }

    @Override
    public OrderDto payment(UUID orderId) {
        Order order = getOrder(orderId);
        order.setState(OrderState.PAID);

        log.debug("Заказ id = '{}' оплачен.", order.getOrderId());
        return orderMapper.toOrderDto(order);
    }

    @Override
    public OrderDto failedPayment(UUID orderId) {
        Order order = getOrder(orderId);
        order.setState(OrderState.PAYMENT_FAILED);

        log.debug("У заказа id = '{}' провалена оплата.", order.getOrderId());
        return orderMapper.toOrderDto(order);
    }

    @Override
    public OrderDto delivery(UUID orderId) {
        Order order = getOrder(orderId);
        order.setState(OrderState.DELIVERED);

        log.debug("Заказ id = '{}' успешно доставлен.", order.getOrderId());
        return orderMapper.toOrderDto(order);
    }

    @Override
    public OrderDto failedDelivery(UUID orderId) {
        Order order = getOrder(orderId);
        order.setState(OrderState.DELIVERY_FAILED);

        log.debug("Заказ id = '{}' не был доставлен.", order.getOrderId());
        return orderMapper.toOrderDto(order);
    }

    @Override
    public OrderDto completeOrder(UUID orderId) {
        Order order = getOrder(orderId);
        order.setState(OrderState.COMPLETED);

        log.debug("Заказ id = '{}' успешно завершен.", order.getOrderId());
        return orderMapper.toOrderDto(order);
    }

    @Override
    public OrderDto calculateTotal(UUID orderId) {
        Order order = getOrder(orderId);
        order.setTotalPrice(paymentClient.getTotalCost(orderMapper.toOrderDto(order)));

        log.debug("Заказ id = '{}' вычислил полную стоимость заказа {}.", order.getOrderId(), order.getTotalPrice());
        return orderMapper.toOrderDto(order);
    }

    @Override
    public OrderDto calculateDelivery(UUID orderId) {
        Order order = getOrder(orderId);
        order.setDeliveryPrice(deliveryClient.deliveryCost(orderMapper.toOrderDto(order)));

        log.debug("Заказ id = '{}' вычислил стоимость доставки {}.", order.getOrderId(), order.getDeliveryPrice());
        return orderMapper.toOrderDto(order);
    }

    @Override
    public OrderDto assembly(UUID orderId) {
        Order order = getOrder(orderId);
        order.setState(OrderState.ASSEMBLED);

        log.debug("Заказ id = '{}' был собран.", order.getOrderId());
        return orderMapper.toOrderDto(order);
    }

    @Override
    public OrderDto failedAssembly(UUID orderId) {
        Order order = getOrder(orderId);
        order.setState(OrderState.ASSEMBLY_FAILED);

        log.debug("Заказ id = '{}' сборка неудачна.", order.getOrderId());
        return orderMapper.toOrderDto(order);
    }

    @Override
    public List<OrderDto> getOrders(String username, Integer page, Integer size) {
        if (username.isBlank()) {
            log.error("Имя пользователя не может быть пустым.");
            throw new NotAuthorizedUserException("Имя пользователя не может быть пустым.");
        }

        ShoppingCartDto shoppingCart = shoppingCartClient.getShoppingCart(username);

        Sort sortByCreated = Sort.by(Sort.Direction.DESC, "created");

        PageRequest pageRequest = PageRequest.of(page, size, sortByCreated);

        Page<Order> orders = orderRepository.findByShoppingCartId(shoppingCart.getShoppingCartId(), pageRequest);

        return orderMapper.toOrdersDto(orders.getContent());
    }

    private Order getOrder(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("Заказ с id: {} не был найден.", orderId);
                    return new OrderNotFoundException("Заказ с id: " + orderId + " не был найден.");
                });
    }

}