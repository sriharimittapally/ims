package com.infosys.ims.entity;

import com.infosys.ims.enums.WarehouseStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "warehouses")
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private String address;

    private String city;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WarehouseStatus status;

    /**
     * The manager assigned to this warehouse.
     * One warehouse → one manager.
     */
    @ManyToOne
    @JoinColumn(name = "manager_id")
    @ToString.Exclude
    private Users manager;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = WarehouseStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}