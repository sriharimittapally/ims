package com.infosys.ims.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "inventory",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"product_id", "warehouse_id"})
        }
)
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Integer reservedQuantity;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.quantity == null) this.quantity = 0;
        if (this.reservedQuantity == null) this.reservedQuantity = 0;
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // availableQuantity is NOT persisted – computed only
    @Transient
    public int getAvailableQuantity() {
        return this.quantity - this.reservedQuantity;
    }
}