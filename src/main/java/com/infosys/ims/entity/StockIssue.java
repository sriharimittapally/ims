package com.infosys.ims.entity;

import com.infosys.ims.enums.StockIssueStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "stock_issues")
public class StockIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String issueNumber;

    @ManyToOne
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne
    @JoinColumn(name = "issued_by", nullable = false)
    private Users issuedBy;

    @ManyToOne
    @JoinColumn(name = "approved_by")
    private Users approvedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StockIssueStatus status;

    @Column(columnDefinition = "TEXT")
    private String note;

    private String rejectionReason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime approvedAt;

    // Set only when status goes to ISSUED
    private LocalDateTime issuedAt;

    @OneToMany(
            mappedBy = "stockIssue",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER

    )
    private List<StockIssueItem> items = new ArrayList<>();

    @PrePersist
    protected void prePersist() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = StockIssueStatus.DRAFT;  // changed from PENDING
    }
}