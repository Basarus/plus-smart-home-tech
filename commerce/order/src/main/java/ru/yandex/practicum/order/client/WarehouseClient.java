package ru.yandex.practicum.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.interactionapi.api.WarehouseOperations;

@FeignClient(name = "warehouse")
public interface WarehouseClient extends WarehouseOperations {
}