package ru.yandex.practicum.warehouse.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "order_bookings")
public class OrderBooking {

    @Id
    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "delivery_id")
    private UUID deliveryId;

    @Column(name = "delivery_weight", nullable = false)
    private double deliveryWeight;

    @Column(name = "delivery_volume", nullable = false)
    private double deliveryVolume;

    @Column(name = "fragile", nullable = false)
    private boolean fragile;

    public OrderBooking() {
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public UUID getDeliveryId() {
        return deliveryId;
    }

    public void setDeliveryId(UUID deliveryId) {
        this.deliveryId = deliveryId;
    }

    public double getDeliveryWeight() {
        return deliveryWeight;
    }

    public void setDeliveryWeight(double deliveryWeight) {
        this.deliveryWeight = deliveryWeight;
    }

    public double getDeliveryVolume() {
        return deliveryVolume;
    }

    public void setDeliveryVolume(double deliveryVolume) {
        this.deliveryVolume = deliveryVolume;
    }

    public boolean isFragile() {
        return fragile;
    }

    public void setFragile(boolean fragile) {
        this.fragile = fragile;
    }
}