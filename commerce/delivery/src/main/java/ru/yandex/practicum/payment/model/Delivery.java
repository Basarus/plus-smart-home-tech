package ru.yandex.practicum.delivery.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import ru.yandex.practicum.interactionapi.enums.DeliveryState;

import java.util.UUID;

@Entity
@Table(name = "deliveries")
public class Delivery {

    @Id
    @Column(name = "delivery_id", nullable = false)
    private UUID deliveryId;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "from_country")
    private String fromCountry;

    @Column(name = "from_city")
    private String fromCity;

    @Column(name = "from_street")
    private String fromStreet;

    @Column(name = "from_house")
    private String fromHouse;

    @Column(name = "from_flat")
    private String fromFlat;

    @Column(name = "to_country")
    private String toCountry;

    @Column(name = "to_city")
    private String toCity;

    @Column(name = "to_street")
    private String toStreet;

    @Column(name = "to_house")
    private String toHouse;

    @Column(name = "to_flat")
    private String toFlat;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_state", nullable = false)
    private DeliveryState deliveryState;

    public UUID getDeliveryId() {
        return deliveryId;
    }

    public void setDeliveryId(UUID deliveryId) {
        this.deliveryId = deliveryId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public String getFromCountry() {
        return fromCountry;
    }

    public void setFromCountry(String fromCountry) {
        this.fromCountry = fromCountry;
    }

    public String getFromCity() {
        return fromCity;
    }

    public void setFromCity(String fromCity) {
        this.fromCity = fromCity;
    }

    public String getFromStreet() {
        return fromStreet;
    }

    public void setFromStreet(String fromStreet) {
        this.fromStreet = fromStreet;
    }

    public String getFromHouse() {
        return fromHouse;
    }

    public void setFromHouse(String fromHouse) {
        this.fromHouse = fromHouse;
    }

    public String getFromFlat() {
        return fromFlat;
    }

    public void setFromFlat(String fromFlat) {
        this.fromFlat = fromFlat;
    }

    public String getToCountry() {
        return toCountry;
    }

    public void setToCountry(String toCountry) {
        this.toCountry = toCountry;
    }

    public String getToCity() {
        return toCity;
    }

    public void setToCity(String toCity) {
        this.toCity = toCity;
    }

    public String getToStreet() {
        return toStreet;
    }

    public void setToStreet(String toStreet) {
        this.toStreet = toStreet;
    }

    public String getToHouse() {
        return toHouse;
    }

    public void setToHouse(String toHouse) {
        this.toHouse = toHouse;
    }

    public String getToFlat() {
        return toFlat;
    }

    public void setToFlat(String toFlat) {
        this.toFlat = toFlat;
    }

    public DeliveryState getDeliveryState() {
        return deliveryState;
    }

    public void setDeliveryState(DeliveryState deliveryState) {
        this.deliveryState = deliveryState;
    }
}