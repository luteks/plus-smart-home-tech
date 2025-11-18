package ru.yandex.practicum.feign;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.dto.AddressDto;
import ru.yandex.practicum.dto.BookedProductsDto;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.request.AddProductToWarehouseRequest;
import ru.yandex.practicum.request.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.request.RegistrationProductInWarehouseRequest;
import ru.yandex.practicum.request.ShippedToDeliveryRequest;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "warehouse")
public interface WarehouseClient {

    @PutMapping("/api/v1/warehouse")
    void newProductInWarehouse(@RequestBody @Valid RegistrationProductInWarehouseRequest requestDto);

    @PostMapping("/api/v1/warehouse/check")
    BookedProductsDto checkProductQuantityEnoughForShoppingCart(@RequestBody @Valid ShoppingCartDto shoppingCartDto);

    @PostMapping("/api/v1/warehouse/add")
    void addProductToWarehouse(@RequestBody @Valid AddProductToWarehouseRequest requestDto);

    @GetMapping("/api/v1/warehouse/address")
    AddressDto getAddress();

    @PostMapping("/api/v1/shipped")
    void loadedToDelivery(ShippedToDeliveryRequest deliveryRequest);

    @PostMapping("/api/v1/return")
    void acceptReturn(@RequestBody Map<UUID, Long> products);

    @PostMapping("/api/v1/assembly")
    BookedProductsDto assemblyProductForOrder(@RequestBody @Valid AssemblyProductsForOrderRequest assemblyProductsForOrder);
}