package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.AddressDto;
import ru.yandex.practicum.dto.BookedProductsDto;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.request.AddProductToWarehouseRequest;
import ru.yandex.practicum.request.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.request.RegistrationProductInWarehouseRequest;
import ru.yandex.practicum.request.ShippedToDeliveryRequest;

import java.util.Map;
import java.util.UUID;

public interface WarehouseService {
    void registrateNewProductInWarehouse(RegistrationProductInWarehouseRequest registrationProductInWarehouseRequest);

    void loadedToDelivery(ShippedToDeliveryRequest deliveryRequest);

    void acceptReturn(Map<UUID, Long> products);

    BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto shoppingCartDto);

    BookedProductsDto assemblyProductsForOrder(AssemblyProductsForOrderRequest assemblyProductsForOrder);

    void addProductToWarehouse(AddProductToWarehouseRequest addProductToWarehouseRequest);

    AddressDto getAddress();
}
