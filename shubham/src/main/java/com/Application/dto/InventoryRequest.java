package com.Application.dto;

import com.Application.entity.type.InventoryCategory;
import com.Application.entity.type.InventoryStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.time.LocalDate;

@Data
public class InventoryRequest {

    private String itemName;
    private InventoryCategory category;

    private Integer quantity;

    private Integer reorderLevel;

    private LocalDate expirationDate;
}
