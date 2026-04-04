package ru.yandex.practicum.delivery.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.interactionapi.api.WarehouseOperations;

@FeignClient(name = "warehouse")
public interface WarehouseClient extends WarehouseOperations {
}