package com.Application.entity;

import com.Application.entity.type.InventoryCategory;
import com.Application.entity.type.InventoryStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String itemName;

    @Enumerated(EnumType.STRING)
    private InventoryCategory category;

    @Enumerated(EnumType.STRING)
    private InventoryStatus status;

    private Integer quantity;

    private Integer reorderLevel;

    private LocalDate expirationDate;
}
