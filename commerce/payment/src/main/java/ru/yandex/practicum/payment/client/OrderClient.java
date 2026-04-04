package ru.yandex.practicum.payment.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.interactionapi.api.OrderOperations;

@FeignClient(name = "order")
public interface OrderClient extends OrderOperations {
}