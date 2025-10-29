package ru.yandex.practicum.feign;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.request.ChangeProductQuantityRequest;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "shopping-cart")
public interface ShoppingCartClient {
    @PutMapping("/api/v1/shopping-cart")
    ShoppingCartDto addProductToShoppingCart(@RequestParam @NotBlank String username, @RequestBody Map<UUID, Long> request);

    @PostMapping("/api/v1/shopping-cart/remove")
    ShoppingCartDto removeFromShoppingCart(@RequestParam @NotBlank String username, @RequestBody Map<UUID, Long> request);

    @PostMapping("/api/v1/shopping-cart/change-quantity")
    ShoppingCartDto changeProductQuantity(@RequestParam @NotBlank String username, @RequestBody @Valid ChangeProductQuantityRequest requestDto);

    @GetMapping("/api/v1/shopping-cart")
    ShoppingCartDto getShoppingCart(@RequestParam @NotBlank String username);

    @DeleteMapping("/api/v1/shopping-cart")
    void deactivateCurrentShoppingCart(@RequestParam @NotBlank String username);
}