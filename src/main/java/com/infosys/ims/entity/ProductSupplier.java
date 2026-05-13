package com.infosys.ims.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "product_suppliers",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"product_id", "supplier_id"})
        }
)
public class ProductSupplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    // The price this supplier charges for this product
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal purchasePrice;

    private Integer leadTimeDays;

    @Column(nullable = false)
    private Boolean isPreferred;

    @Column(nullable = false)
    private Boolean isActive;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.isActive == null) this.isActive = true;
        if (this.isPreferred == null) this.isPreferred = false;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}