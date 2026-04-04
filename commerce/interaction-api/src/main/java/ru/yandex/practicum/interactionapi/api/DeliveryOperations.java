package ru.yandex.practicum.interactionapi.api;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.interactionapi.dto.order.OrderDto;
import ru.yandex.practicum.interactionapi.dto.warehouse.AddressDto;
import ru.yandex.practicum.interactionapi.enums.DeliveryState;

import java.math.BigDecimal;
import java.util.UUID;

@RequestMapping("/api/v1/delivery")
public interface DeliveryOperations {

    @PutMapping
    ru.yandex.practicum.interactionapi.dto.delivery.DeliveryDto planDelivery(
            @RequestBody ru.yandex.practicum.interactionapi.dto.delivery.DeliveryDto deliveryDto
    );

    @PostMapping("/cost")
    BigDecimal deliveryCost(@RequestBody OrderDto orderDto);

    @PostMapping("/picked")
    void deliveryPicked(@RequestBody UUID deliveryId);

    @PostMapping("/successful")
    void deliverySuccessful(@RequestBody UUID deliveryId);

    @PostMapping("/failed")
    void deliveryFailed(@RequestBody UUID deliveryId);
}