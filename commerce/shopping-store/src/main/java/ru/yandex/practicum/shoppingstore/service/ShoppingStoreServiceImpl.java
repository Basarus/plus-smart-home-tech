package ru.yandex.practicum.shoppingstore.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.interactionapi.dto.store.ProductDto;
import ru.yandex.practicum.interactionapi.dto.store.SetProductQuantityStateRequest;
import ru.yandex.practicum.interactionapi.enums.ProductCategory;
import ru.yandex.practicum.interactionapi.enums.ProductState;
import ru.yandex.practicum.shoppingstore.exception.ProductNotFoundException;
import ru.yandex.practicum.shoppingstore.mapper.ProductMapper;
import ru.yandex.practicum.shoppingstore.model.Product;
import ru.yandex.practicum.shoppingstore.repository.ProductRepository;

import java.util.UUID;

@Service
@Transactional
public class ShoppingStoreServiceImpl implements ShoppingStoreService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ShoppingStoreServiceImpl(ProductRepository productRepository,
                                    ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto> getProducts(ProductCategory category, Pageable pageable) {
        if (category == null) {
            return productRepository.findAllByProductState(ProductState.ACTIVE, pageable)
                    .map(productMapper::toDto);
        }

        return productRepository.findAllByProductCategoryAndProductState(
                        category,
                        ProductState.ACTIVE,
                        pageable
                )
                .map(productMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto getProductById(UUID productId) {
        Product product = productRepository.findByProductIdAndProductState(productId, ProductState.ACTIVE)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        return productMapper.toDto(product);
    }

    @Override
    public ProductDto createProduct(ProductDto productDto) {
        Product product = productMapper.toEntity(productDto);
        if (product.getProductId() == null) {
            product.setProductId(UUID.randomUUID());
        }
        if (product.getProductState() == null) {
            product.setProductState(ProductState.ACTIVE);
        }
        Product saved = productRepository.save(product);
        return productMapper.toDto(saved);
    }

    @Override
    public ProductDto updateProduct(ProductDto productDto) {
        Product product = productRepository.findById(productDto.productId())
                .orElseThrow(() -> new ProductNotFoundException(productDto.productId()));

        productMapper.update(product, productDto);
        Product saved = productRepository.save(product);
        return productMapper.toDto(saved);
    }

    @Override
    public boolean removeProduct(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        product.setProductState(ProductState.DEACTIVATE);
        productRepository.save(product);
        return true;
    }

    @Override
    public boolean setQuantityState(SetProductQuantityStateRequest request) {
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ProductNotFoundException(request.productId()));

        product.setQuantityState(request.quantityState());
        productRepository.save(product);
        return true;
    }
}