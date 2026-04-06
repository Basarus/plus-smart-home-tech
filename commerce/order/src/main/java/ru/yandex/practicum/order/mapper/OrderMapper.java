package ru.yandex.practicum.order.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.interactionapi.dto.order.OrderDto;
import ru.yandex.practicum.interactionapi.dto.warehouse.AddressDto;
import ru.yandex.practicum.order.model.Order;

@Component
public class OrderMapper {

    public OrderDto toDto(Order order) {
        return new OrderDto(
                order.getOrderId(),
                order.getShoppingCartId(),
                order.getProducts(),
                order.getPaymentId(),
                order.getDeliveryId(),
                order.getState(),
                order.getDeliveryWeight(),
                order.getDeliveryVolume(),
                order.getFragile(),
                order.getTotalPrice(),
                order.getDeliveryPrice(),
                order.getProductPrice(),
                buildFromAddress(order),
                buildToAddress(order)
        );
    }

    public AddressDto buildFromAddress(Order order) {
        return new AddressDto(
                order.getFromCountry(),
                order.getFromCity(),
                order.getFromStreet(),
                order.getFromHouse(),
                order.getFromFlat()
        );
    }

    public AddressDto buildToAddress(Order order) {
        return new AddressDto(
                order.getToCountry(),
                order.getToCity(),
                order.getToStreet(),
                order.getToHouse(),
                order.getToFlat()
        );
    }

    public void mapFromAddress(Order order, AddressDto address) {
        if (address == null) {
            return;
        }
        order.setFromCountry(address.country());
        order.setFromCity(address.city());
        order.setFromStreet(address.street());
        order.setFromHouse(address.house());
        order.setFromFlat(address.flat());
    }

    public void mapToAddress(Order order, AddressDto address) {
        if (address == null) {
            return;
        }
        order.setToCountry(address.country());
        order.setToCity(address.city());
        order.setToStreet(address.street());
        order.setToHouse(address.house());
        order.setToFlat(address.flat());
    }
}