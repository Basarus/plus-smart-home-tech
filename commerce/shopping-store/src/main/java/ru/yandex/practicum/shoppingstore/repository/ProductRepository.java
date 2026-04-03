package ru.yandex.practicum.shoppingstore.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.interactionapi.enums.ProductCategory;
import ru.yandex.practicum.interactionapi.enums.ProductState;
import ru.yandex.practicum.shoppingstore.model.Product;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findByProductIdAndProductState(UUID productId, ProductState productState);

    Page<Product> findAllByProductState(ProductState productState, Pageable pageable);

    Page<Product> findAllByProductCategoryAndProductState(
            ProductCategory productCategory,
            ProductState productState,
            Pageable pageable
    );
}