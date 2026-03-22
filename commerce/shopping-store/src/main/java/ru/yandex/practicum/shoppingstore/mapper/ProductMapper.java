package ru.yandex.practicum.shoppingstore.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.yandex.practicum.interactionapi.dto.store.ProductDto;
import ru.yandex.practicum.shoppingstore.model.Product;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", imports = BigDecimal.class)
public interface ProductMapper {

    @Mapping(target = "price", expression = "java(product.getPrice() != null ? product.getPrice().doubleValue() : null)")
    ProductDto toDto(Product product);

    @Mapping(target = "price", expression = "java(dto.price() != null ? BigDecimal.valueOf(dto.price()) : null)")
    Product toEntity(ProductDto dto);

    @Mapping(target = "productId", ignore = true)
    @Mapping(target = "price", expression = "java(dto.price() != null ? BigDecimal.valueOf(dto.price()) : null)")
    void update(@MappingTarget Product product, ProductDto dto);
}