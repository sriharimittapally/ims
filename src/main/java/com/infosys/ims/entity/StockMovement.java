package com.infosys.ims.entity;

import com.infosys.ims.enums.StockMovementReferenceType;
import com.infosys.ims.enums.StockMovementType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "stock_movements")
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StockMovementType type;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Integer quantityAfter;

    private Long referenceId;

    @Enumerated(EnumType.STRING)
    private StockMovementReferenceType referenceType;

    private String note;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private Users createdBy;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}