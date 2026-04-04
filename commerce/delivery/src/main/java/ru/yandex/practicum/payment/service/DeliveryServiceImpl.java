package ru.yandex.practicum.delivery.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.delivery.client.OrderClient;
import ru.yandex.practicum.delivery.client.WarehouseClient;
import ru.yandex.practicum.delivery.exception.NoDeliveryFoundException;
import ru.yandex.practicum.delivery.model.Delivery;
import ru.yandex.practicum.delivery.repository.DeliveryRepository;
import ru.yandex.practicum.interactionapi.dto.delivery.DeliveryDto;
import ru.yandex.practicum.interactionapi.dto.order.OrderDto;
import ru.yandex.practicum.interactionapi.dto.warehouse.AddressDto;
import ru.yandex.practicum.interactionapi.dto.warehouse.ShippedToDeliveryRequest;
import ru.yandex.practicum.interactionapi.enums.DeliveryState;

import java.util.UUID;

@Service
@Transactional
public class DeliveryServiceImpl implements DeliveryService {

    private static final double BASE_COST = 5.0;

    private final DeliveryRepository deliveryRepository;
    private final OrderClient orderClient;
    private final WarehouseClient warehouseClient;

    public DeliveryServiceImpl(DeliveryRepository deliveryRepository,
                               OrderClient orderClient,
                               WarehouseClient warehouseClient) {
        this.deliveryRepository = deliveryRepository;
        this.orderClient = orderClient;
        this.warehouseClient = warehouseClient;
    }

    @Override
    public DeliveryDto planDelivery(DeliveryDto deliveryDto) {
        Delivery delivery = new Delivery();
        delivery.setDeliveryId(
                deliveryDto.deliveryId() != null ? deliveryDto.deliveryId() : UUID.randomUUID()
        );
        delivery.setOrderId(deliveryDto.orderId());

        mapFromAddress(delivery, deliveryDto.fromAddress());
        mapToAddress(delivery, deliveryDto.toAddress());

        delivery.setDeliveryState(
                deliveryDto.deliveryState() != null ? deliveryDto.deliveryState() : DeliveryState.CREATED
        );

        deliveryRepository.save(delivery);
        return toDto(delivery);
    }

    @Override
    @Transactional(readOnly = true)
    public Double deliveryCost(OrderDto orderDto) {
        double total = BASE_COST;

        String warehouseAddress = extractWarehouseAddress(orderDto.fromAddress());
        if (warehouseAddress.contains("ADDRESS_2")) {
            total += BASE_COST * 2;
        } else {
            total += BASE_COST;
        }

        if (Boolean.TRUE.equals(orderDto.fragile())) {
            total += total * 0.2;
        }

        total += safe(orderDto.deliveryWeight()) * 0.3;
        total += safe(orderDto.deliveryVolume()) * 0.2;

        String fromStreet = orderDto.fromAddress() != null ? orderDto.fromAddress().street() : null;
        String toStreet = orderDto.toAddress() != null ? orderDto.toAddress().street() : null;

        if (fromStreet != null && toStreet != null && !fromStreet.equalsIgnoreCase(toStreet)) {
            total += total * 0.2;
        }

        return total;
    }

    @Override
    public void deliveryPicked(UUID deliveryId) {
        Delivery delivery = getDelivery(deliveryId);
        delivery.setDeliveryState(DeliveryState.IN_PROGRESS);
        deliveryRepository.save(delivery);

        warehouseClient.shippedToDelivery(
                new ShippedToDeliveryRequest(delivery.getOrderId(), delivery.getDeliveryId())
        );

        orderClient.assembly(delivery.getOrderId());
    }

    @Override
    public void deliverySuccessful(UUID deliveryId) {
        Delivery delivery = getDelivery(deliveryId);
        delivery.setDeliveryState(DeliveryState.DELIVERED);
        deliveryRepository.save(delivery);

        orderClient.delivery(delivery.getOrderId());
    }

    @Override
    public void deliveryFailed(UUID deliveryId) {
        Delivery delivery = getDelivery(deliveryId);
        delivery.setDeliveryState(DeliveryState.FAILED);
        deliveryRepository.save(delivery);

        orderClient.deliveryFailed(delivery.getOrderId());
    }

    private Delivery getDelivery(UUID deliveryId) {
        return deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new NoDeliveryFoundException(deliveryId));
    }

    private DeliveryDto toDto(Delivery delivery) {
        return new DeliveryDto(
                delivery.getDeliveryId(),
                new AddressDto(
                        delivery.getFromCountry(),
                        delivery.getFromCity(),
                        delivery.getFromStreet(),
                        delivery.getFromHouse(),
                        delivery.getFromFlat()
                ),
                new AddressDto(
                        delivery.getToCountry(),
                        delivery.getToCity(),
                        delivery.getToStreet(),
                        delivery.getToHouse(),
                        delivery.getToFlat()
                ),
                delivery.getOrderId(),
                delivery.getDeliveryState()
        );
    }

    private void mapFromAddress(Delivery delivery, AddressDto address) {
        if (address == null) {
            return;
        }
        delivery.setFromCountry(address.country());
        delivery.setFromCity(address.city());
        delivery.setFromStreet(address.street());
        delivery.setFromHouse(address.house());
        delivery.setFromFlat(address.flat());
    }

    private void mapToAddress(Delivery delivery, AddressDto address) {
        if (address == null) {
            return;
        }
        delivery.setToCountry(address.country());
        delivery.setToCity(address.city());
        delivery.setToStreet(address.street());
        delivery.setToHouse(address.house());
        delivery.setToFlat(address.flat());
    }

    private String extractWarehouseAddress(AddressDto address) {
        if (address == null) {
            return "";
        }
        String country = address.country() == null ? "" : address.country();
        String city = address.city() == null ? "" : address.city();
        String street = address.street() == null ? "" : address.street();
        String house = address.house() == null ? "" : address.house();
        String flat = address.flat() == null ? "" : address.flat();

        return String.join(" ", country, city, street, house, flat);
    }

    private double safe(Double value) {
        return value == null ? 0.0 : value;
    }
}