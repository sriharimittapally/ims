package com.infosys.ims.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "stock_issue_items")
public class StockIssueItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_issue_id", nullable = false)
    private StockIssue stockIssue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantityRequested;

    // Actual issued quantity (may differ if partial issue is done)
    @Column(nullable = false)
    private Integer quantityIssued;

    @PrePersist
    protected void prePersist() {
        if (this.quantityIssued == null) {
            this.quantityIssued = this.quantityRequested;
        }
    }
}