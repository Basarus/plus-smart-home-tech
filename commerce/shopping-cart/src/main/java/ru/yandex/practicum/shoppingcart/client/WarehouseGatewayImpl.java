package ru.yandex.practicum.shoppingcart.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.interactionapi.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.interactionapi.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.shoppingcart.exception.WarehouseUnavailableException;

@Component
public class WarehouseGatewayImpl implements WarehouseGateway {

    private final WarehouseClient warehouseClient;

    public WarehouseGatewayImpl(WarehouseClient warehouseClient) {
        this.warehouseClient = warehouseClient;
    }

    @Override
    @CircuitBreaker(name = "warehouse", fallbackMethod = "checkProductsFallback")
    public BookedProductsDto checkProducts(ShoppingCartDto shoppingCartDto) {
        return warehouseClient.checkProducts(shoppingCartDto);
    }

    public BookedProductsDto checkProductsFallback(ShoppingCartDto shoppingCartDto, Throwable throwable) {
        throw new WarehouseUnavailableException();
    }
}