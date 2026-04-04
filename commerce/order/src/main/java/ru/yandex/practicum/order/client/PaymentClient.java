package ru.yandex.practicum.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.interactionapi.api.PaymentOperations;

@FeignClient(name = "payment")
public interface PaymentClient extends PaymentOperations {
}