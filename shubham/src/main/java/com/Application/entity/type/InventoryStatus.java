package com.Application.entity.type;

public enum InventoryStatus {
    AVAILABLE,    // Can be used
    LOW_STOCK,    // Quantity is low
    OUT_OF_STOCK, // Quantity = 0
    EXPIRED
}
