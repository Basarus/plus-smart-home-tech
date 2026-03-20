package ru.yandex.practicum.warehouse.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.interactionapi.dto.warehouse.NewProductInWarehouseRequest;
import ru.yandex.practicum.warehouse.model.WarehouseProduct;

import java.math.BigDecimal;

@Component
public class WarehouseMapper {

    public WarehouseProduct toEntity(NewProductInWarehouseRequest request) {
        WarehouseProduct product = new WarehouseProduct();
        product.setProductId(request.productId());
        product.setFragile(request.fragile());
        product.setWidth(BigDecimal.valueOf(request.dimension().width()));
        product.setHeight(BigDecimal.valueOf(request.dimension().height()));
        product.setDepth(BigDecimal.valueOf(request.dimension().depth()));
        product.setWeight(BigDecimal.valueOf(request.weight()));
        product.setQuantity(request.quantity());
        return product;
    }
}