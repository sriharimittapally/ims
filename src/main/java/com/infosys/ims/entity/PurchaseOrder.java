package com.infosys.ims.entity;

import com.infosys.ims.enums.PurchaseOrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "purchase_orders")
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String poNumber;

    @ManyToOne
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PurchaseOrderStatus status;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private Users createdBy;

    @ManyToOne
    @JoinColumn(name = "received_by")
    private Users receivedBy;

    private String note;

    private String rejectionReason;


    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime shippedAt;
    private LocalDateTime receivedAt;

    @OneToMany(
            mappedBy = "purchaseOrder",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    private List<PurchaseOrderItem> items = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = PurchaseOrderStatus.DRAFT;
        }
        if (this.totalAmount == null) {
            this.totalAmount = BigDecimal.ZERO;
        }
    }
}