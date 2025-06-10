package com.messmanagement.purchase.dto;

import com.messmanagement.menu.dto.MenuItemDTO; // To include menu item details
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseResponseDTO {
    private Long purchaseId;
    private Long userId;
    private String userEmail; // For convenience
    private MenuItemDTO menuItem; // Details of the purchased item
    private Integer quantity;
    private BigDecimal totalAmount;
    private LocalDateTime purchaseDate;
    private String paymentTransactionId;
    // Potentially a payment status if purchases can be pending
}