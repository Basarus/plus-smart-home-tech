package ru.yandex.practicum.warehouse.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "warehouse_products")
public class WarehouseProduct {

    @Id
    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "fragile", nullable = false)
    private boolean fragile;

    @Column(name = "width", nullable = false, precision = 19, scale = 3)
    private BigDecimal width;

    @Column(name = "height", nullable = false, precision = 19, scale = 3)
    private BigDecimal height;

    @Column(name = "depth", nullable = false, precision = 19, scale = 3)
    private BigDecimal depth;

    @Column(name = "weight", nullable = false, precision = 19, scale = 3)
    private BigDecimal weight;

    @Column(name = "quantity", nullable = false)
    private long quantity;

    public WarehouseProduct() {
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public boolean isFragile() {
        return fragile;
    }

    public void setFragile(boolean fragile) {
        this.fragile = fragile;
    }

    public BigDecimal getWidth() {
        return width;
    }

    public void setWidth(BigDecimal width) {
        this.width = width;
    }

    public BigDecimal getHeight() {
        return height;
    }

    public void setHeight(BigDecimal height) {
        this.height = height;
    }

    public BigDecimal getDepth() {
        return depth;
    }

    public void setDepth(BigDecimal depth) {
        this.depth = depth;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }
}