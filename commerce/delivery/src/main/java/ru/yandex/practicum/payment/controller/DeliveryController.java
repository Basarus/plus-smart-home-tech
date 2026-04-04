package ru.yandex.practicum.delivery.controller;

import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.delivery.service.DeliveryService;
import ru.yandex.practicum.interactionapi.api.DeliveryOperations;
import ru.yandex.practicum.interactionapi.dto.delivery.DeliveryDto;
import ru.yandex.practicum.interactionapi.dto.order.OrderDto;

import java.util.UUID;

@RestController
public class DeliveryController implements DeliveryOperations {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @Override
    public DeliveryDto planDelivery(DeliveryDto deliveryDto) {
        return deliveryService.planDelivery(deliveryDto);
    }

    @Override
    public Double deliveryCost(OrderDto orderDto) {
        return deliveryService.deliveryCost(orderDto);
    }

    @Override
    public void deliveryPicked(UUID deliveryId) {
        deliveryService.deliveryPicked(deliveryId);
    }

    @Override
    public void deliverySuccessful(UUID deliveryId) {
        deliveryService.deliverySuccessful(deliveryId);
    }

    @Override
    public void deliveryFailed(UUID deliveryId) {
        deliveryService.deliveryFailed(deliveryId);
    }
}