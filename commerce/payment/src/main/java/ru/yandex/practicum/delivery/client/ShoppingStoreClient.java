package ru.yandex.practicum.delivery.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.interactionapi.api.ShoppingStoreOperations;

@FeignClient(name = "shopping-store")
public interface ShoppingStoreClient extends ShoppingStoreOperations {
}