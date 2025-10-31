package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.AddressDto;
import ru.yandex.practicum.dto.BookedProductsDto;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.request.AddProductToWarehouseRequest;
import ru.yandex.practicum.request.RegistrationProductInWarehouseRequest;

public interface WarehouseService {
    void registrateNewProductInWarehouse(RegistrationProductInWarehouseRequest registrationProductInWarehouseRequest);

    BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto shoppingCartDto);

    void addProductToWarehouse(AddProductToWarehouseRequest addProductToWarehouseRequest);

    AddressDto getAddress();
}
