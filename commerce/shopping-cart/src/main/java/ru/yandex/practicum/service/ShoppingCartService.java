package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.request.ChangeProductQuantityRequest;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ShoppingCartService {
    ShoppingCartDto addProductToShoppingCart(String username, Map<UUID, Integer> request);

    ShoppingCartDto removeFromShoppingCart(String username, List<UUID> productsId);

    ShoppingCartDto changeProductQuantity(String username, ChangeProductQuantityRequest requestDto);

    ShoppingCartDto getShoppingCart(String username);

    void deactivateCurrentShoppingCart(String username);
}
