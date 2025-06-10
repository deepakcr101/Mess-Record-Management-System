package com.messmanagement.purchase.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DishPurchaseRequestDTO {

    @NotNull(message = "Menu item ID cannot be null")
    private Long itemId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity = 1; // Default to 1 if not provided, but @Min(1) implies it should be sent

    // Any other details needed for a purchase request, e.g., special instructions (though not in plan)
}