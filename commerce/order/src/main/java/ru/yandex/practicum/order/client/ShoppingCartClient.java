package ru.yandex.practicum.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.interactionapi.api.ShoppingCartOperations;

@FeignClient(name = "shopping-cart")
public interface ShoppingCartClient extends ShoppingCartOperations {
}