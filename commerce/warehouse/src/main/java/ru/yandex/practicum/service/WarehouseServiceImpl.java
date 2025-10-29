package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.AddressDto;
import ru.yandex.practicum.dto.BookedProductsDto;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.enums.QuantityState;
import ru.yandex.practicum.exception.ProductLowQuantityInWarehouseException;
import ru.yandex.practicum.exception.ProductNotFoundInWarehouseException;
import ru.yandex.practicum.exception.ProductNotUniqueInWarehouseException;
import ru.yandex.practicum.feign.ShoppingStoreClient;
import ru.yandex.practicum.mapper.WarehouseMapper;
import ru.yandex.practicum.model.Address;
import ru.yandex.practicum.model.Warehouse;
import ru.yandex.practicum.repository.WarehouseRepository;
import ru.yandex.practicum.request.AddProductToWarehouseRequest;
import ru.yandex.practicum.request.RegistrationProductInWarehouseRequest;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.READ_COMMITTED)
public class WarehouseServiceImpl implements WarehouseService {
    private final WarehouseRepository warehouseRepository;
    private final WarehouseMapper warehouseMapper;
    private final ShoppingStoreClient shoppingStoreClient;

    @Override
    public void registrateNewProductInWarehouse(RegistrationProductInWarehouseRequest registrationProductInWarehouseRequest) {
        warehouseRepository.findById(registrationProductInWarehouseRequest.getProductId()).ifPresent(warehouse -> {
            log.error("Товар с таким описанием уже зарегистрирован.");
            throw new ProductNotUniqueInWarehouseException("Товар с таким описанием уже зарегистрирован.");
        });

        Warehouse warehouse = warehouseMapper.toWarehouse(registrationProductInWarehouseRequest);
        warehouseRepository.save(warehouse);
        warehouseRepository.flush();
        log.info("Товар успешно добавлен в cписок товаров склада.");
    }

    @Override
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto shoppingCartDto) {
        Map<UUID, Integer> products = shoppingCartDto.getProducts();
        Set<UUID> cartProductIds = products.keySet();

        Map<UUID, Warehouse> warehouseProducts = warehouseRepository.findAllById(cartProductIds)
                .stream()
                .collect(Collectors.toMap(Warehouse::getProductId, Function.identity()));

        Set<UUID> productIds = warehouseProducts.keySet();

        cartProductIds.forEach(id -> {
            if (!productIds.contains(id)) {
                log.error("Товар не найден на складе.");
                throw new ProductNotFoundInWarehouseException("Товар не найден на складе.");
            }
        });

        products.forEach((key, value) -> {
            if (warehouseProducts.get(key).getQuantity() < value) {
                log.error("На складе малое количество товаров.");
                throw new ProductLowQuantityInWarehouseException("На складе малое количество товаров.");
            }
        });

        BookedProductsDto bookedProductsDto = getBookedProducts(warehouseProducts.values(), products);

        log.info("Возвращено количество товара на складе.");
        return bookedProductsDto;
    }

    @Override
    public void addProductToWarehouse(AddProductToWarehouseRequest addProductToWarehouseRequest) {
        Warehouse warehouse = warehouseRepository.findById(addProductToWarehouseRequest.getProductId()).orElseThrow(
                () -> {
                    log.error("Информация на складе о товаре отсутствует.");
                    return new ProductNotFoundInWarehouseException("Информация на складе о товаре отсутствует.");
                }
        );

        warehouse.setQuantity(warehouse.getQuantity() + addProductToWarehouseRequest.getQuantity());
        updateProductQuantityInShoppingStore(warehouse);

        log.info("Обновлено количество товара на складе.");
    }

    @Override
    public AddressDto getAddress() {
        String address = Address.CURRENT_ADDRESS;
        return AddressDto.builder()
                .country(address)
                .city(address)
                .street(address)
                .house(address)
                .flat(address)
                .build();
    }

    private BookedProductsDto getBookedProducts(Collection<Warehouse> productList, Map<UUID, Integer> cartProducts) {
        return BookedProductsDto.builder()
                .fragile(productList.stream().anyMatch(Warehouse::getFragile))
                .deliveryWeight(calculateTotalWeight(productList, cartProducts))
                .deliveryVolume(calculateTotalVolume(productList, cartProducts))
                .build();
    }

    private double calculateTotalWeight(Collection<Warehouse> productList, Map<UUID, Integer> cartProducts) {
        return productList.stream()
                .mapToDouble(prod -> multiply(prod.getWeight(), getQuantity(prod, cartProducts)))
                .sum();
    }

    private double calculateTotalVolume(Collection<Warehouse> productList, Map<UUID, Integer> cartProducts) {
        return productList.stream()
                .mapToDouble(prod -> {
                    double width = getValue(prod.getWidth());
                    double height = getValue(prod.getHeight());
                    double depth = getValue(prod.getDepth());
                    int quantity = getQuantity(prod, cartProducts);
                    return width * height * depth * quantity;
                }).sum();
    }

    private void updateProductQuantityInShoppingStore(Warehouse product) {
        UUID productId = product.getProductId();
        QuantityState quantityState;

        if (product.getQuantity() == 0) {
            quantityState = QuantityState.ENDED;
        } else if (product.getQuantity() < 10) {
            quantityState = QuantityState.ENOUGH;
        } else if (product.getQuantity() < 100) {
            quantityState = QuantityState.FEW;
        } else {
            quantityState = QuantityState.MANY;
        }

        shoppingStoreClient.setProductQuantityState(productId, quantityState);
    }

    private double getValue(Double value) {
        return value != null ? value : 0.0;
    }

    private int getQuantity(Warehouse prod, Map<UUID, Integer> cartProducts) {
        return cartProducts.getOrDefault(prod.getProductId(), 0);
    }

    private double multiply(Double value, int multiplier) {
        return value != null ? value * multiplier : 0;
    }
}