package com.infosys.ims.entity;

import com.infosys.ims.enums.ApprovalStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "suppliers")
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    private String companyName;
    private String address;
    private String gstNumber;
    private String phone;

    // Categories this supplier is allowed to supply
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "supplier_categories",
            joinColumns = @JoinColumn(name = "supplier_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<Category> categories;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalStatus approvalStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private Users reviewedBy;

    private String rejectionReason;

    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.approvalStatus == null) {
            this.approvalStatus = ApprovalStatus.PENDING;
        }
    }
}