package ru.yandex.practicum.warehouse.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.interactionapi.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.interactionapi.dto.warehouse.*;
import ru.yandex.practicum.warehouse.exception.NoOrderBookingFoundException;
import ru.yandex.practicum.warehouse.exception.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.warehouse.exception.ProductInShoppingCartLowQuantityInWarehouseException;
import ru.yandex.practicum.warehouse.exception.SpecifiedProductAlreadyInWarehouseException;
import ru.yandex.practicum.warehouse.mapper.WarehouseMapper;
import ru.yandex.practicum.warehouse.model.OrderBooking;
import ru.yandex.practicum.warehouse.model.WarehouseProduct;
import ru.yandex.practicum.warehouse.repository.OrderBookingRepository;
import ru.yandex.practicum.warehouse.repository.WarehouseProductRepository;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Service
@Transactional
public class WarehouseServiceImpl implements WarehouseService {

    private static final String[] ADDRESSES = new String[]{"ADDRESS_1", "ADDRESS_2"};
    private static final String CURRENT_ADDRESS =
            ADDRESSES[Random.from(new SecureRandom()).nextInt(0, ADDRESSES.length)];

    private final WarehouseProductRepository warehouseProductRepository;
    private final OrderBookingRepository orderBookingRepository;
    private final WarehouseMapper warehouseMapper;

    public WarehouseServiceImpl(WarehouseProductRepository warehouseProductRepository,
                                OrderBookingRepository orderBookingRepository,
                                WarehouseMapper warehouseMapper) {
        this.warehouseProductRepository = warehouseProductRepository;
        this.orderBookingRepository = orderBookingRepository;
        this.warehouseMapper = warehouseMapper;
    }

    @Override
    public void createProduct(NewProductInWarehouseRequest request) {
        if (warehouseProductRepository.existsById(request.productId())) {
            throw new SpecifiedProductAlreadyInWarehouseException(request.productId());
        }

        WarehouseProduct product = warehouseMapper.toEntity(request);
        warehouseProductRepository.save(product);
    }

    @Override
    public void addQuantity(AddProductToWarehouseRequest request) {
        WarehouseProduct product = warehouseProductRepository.findById(request.productId())
                .orElseThrow(() -> new NoSpecifiedProductInWarehouseException(request.productId()));

        product.setQuantity(product.getQuantity() + request.quantity());
        warehouseProductRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public BookedProductsDto checkProducts(ShoppingCartDto shoppingCartDto) {
        return calculateBooking(shoppingCartDto.products(), false, null);
    }

    @Override
    public AddressDto getAddress() {
        return new AddressDto(
                CURRENT_ADDRESS,
                CURRENT_ADDRESS,
                CURRENT_ADDRESS,
                CURRENT_ADDRESS,
                CURRENT_ADDRESS
        );
    }

    @Override
    public BookedProductsDto assemblyProductsForOrder(AssemblyProductsForOrderRequest request) {
        BookedProductsDto bookedProductsDto = calculateBooking(request.products(), true, request.orderId());

        OrderBooking orderBooking = new OrderBooking();
        orderBooking.setOrderId(request.orderId());
        orderBooking.setDeliveryWeight(bookedProductsDto.deliveryWeight());
        orderBooking.setDeliveryVolume(bookedProductsDto.deliveryVolume());
        orderBooking.setFragile(bookedProductsDto.fragile());
        orderBookingRepository.save(orderBooking);

        return bookedProductsDto;
    }

    @Override
    public void shippedToDelivery(ShippedToDeliveryRequest request) {
        OrderBooking orderBooking = orderBookingRepository.findById(request.orderId())
                .orElseThrow(() -> new NoOrderBookingFoundException(request.orderId()));

        orderBooking.setDeliveryId(request.deliveryId());
        orderBookingRepository.save(orderBooking);
    }

    @Override
    public void acceptReturn(Map<UUID, Long> products) {
        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            UUID productId = entry.getKey();
            long quantityToReturn = entry.getValue();

            WarehouseProduct product = warehouseProductRepository.findById(productId)
                    .orElseThrow(() -> new NoSpecifiedProductInWarehouseException(productId));

            product.setQuantity(product.getQuantity() + quantityToReturn);
            warehouseProductRepository.save(product);
        }
    }

    private BookedProductsDto calculateBooking(Map<UUID, Long> products,
                                               boolean decreaseQuantity,
                                               UUID orderId) {
        double totalWeight = 0.0;
        double totalVolume = 0.0;
        boolean fragile = false;

        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            UUID productId = entry.getKey();
            long requestedQuantity = entry.getValue();

            WarehouseProduct product = warehouseProductRepository.findById(productId)
                    .orElseThrow(() -> new NoSpecifiedProductInWarehouseException(productId));

            if (product.getQuantity() < requestedQuantity) {
                throw new ProductInShoppingCartLowQuantityInWarehouseException(
                        productId,
                        requestedQuantity,
                        product.getQuantity()
                );
            }

            if (decreaseQuantity) {
                product.setQuantity(product.getQuantity() - requestedQuantity);
                warehouseProductRepository.save(product);
            }

            totalWeight += product.getWeight().doubleValue() * requestedQuantity;
            totalVolume += product.getWidth().doubleValue()
                    * product.getHeight().doubleValue()
                    * product.getDepth().doubleValue()
                    * requestedQuantity;

            if (product.isFragile()) {
                fragile = true;
            }
        }

        return new BookedProductsDto(totalWeight, totalVolume, fragile);
    }
}