package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.enums.ProductCategory;
import ru.yandex.practicum.enums.ProductState;
import ru.yandex.practicum.exception.ProductNotFoundException;
import ru.yandex.practicum.mapper.ProductMapper;
import ru.yandex.practicum.model.Product;
import ru.yandex.practicum.repository.ShoppingStoreRepository;
import ru.yandex.practicum.request.SetProductQuantityStateRequest;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ShoppingStoreServiceImpl implements ShoppingStoreService {
    private final ShoppingStoreRepository storeRepository;
    private final ProductMapper productMapper;

    @Override
    public ProductDto createNewProduct(ProductDto productDto) {
        Product newProduct = productMapper.productDtoToProduct(productDto);
        ProductDto newProductDto = productMapper.productToProductDto(storeRepository.save(newProduct));
        log.info("Новый товар добавлен: {}", newProductDto);
        return newProductDto;
    }

    @Override
    public ProductDto updateProduct(ProductDto productDto) {
        Product oldProduct = getProductFromRepository(productDto.getProductId());
        Product newProduct = productMapper.productDtoToProduct(productDto);
        newProduct.setProductId(oldProduct.getProductId());
        ProductDto newProductDto = productMapper.productToProductDto(storeRepository.save(newProduct));
        log.info("Товар id '{}' успешно изменен.", productDto.getProductId());
        return newProductDto;
    }

    @Override
    public boolean removeProductFromStore(UUID productId) {
        Product product = getProductFromRepository(productId);
        product.setProductState(ProductState.DEACTIVATE);

        log.info("Товар id '{}' снят с продажи.", productId);
        return true;
    }

    @Override
    public boolean setProductQuantityState(SetProductQuantityStateRequest request) {
        if (storeRepository.findById(request.getProductId()).isEmpty()) {
            return false;
        }

        Product product = getProductFromRepository(request.getProductId());

        if (!product.getQuantityState().equals(request.getQuantityState())) {
            product.setQuantityState(request.getQuantityState());
            storeRepository.save(product);
        }

        log.info("Количество товара id '{}' успешно изменено.", request.getProductId());
        return true;
    }

    @Transactional(readOnly = true)
    @Override
    public Page<ProductDto> getProducts(ProductCategory productCategory, Pageable pageableDto) {
        Sort sort = pageableDto.getSort();

        if (sort.isEmpty()) {
            sort = Sort.by(Sort.Direction.ASC, "productName");
        }

        PageRequest pageRequest = PageRequest.of(
                pageableDto.getPageNumber(),
                pageableDto.getPageSize(),
                sort
        );

        Page<Product> products = storeRepository.findAllByProductCategory(
                productCategory,
                pageRequest
        );

        Page<ProductDto> productDtos = products.map(productMapper::productToProductDto);

        log.info("Возвращен список всех продуктов: {}", productDtos);
        return productDtos;
    }

    @Override
    public ProductDto getProduct(UUID productId) {
        Product product = getProductFromRepository(productId);

        return productMapper.productToProductDto(product);
    }

    private Product getProductFromRepository(UUID productId) {
        return storeRepository.findById(productId).orElseThrow(
                () -> {
                    log.error("Товар с id {} не найден.", productId);
                    return new ProductNotFoundException("Товар с id: " + productId + " не найден.");
                }
        );
    }
}