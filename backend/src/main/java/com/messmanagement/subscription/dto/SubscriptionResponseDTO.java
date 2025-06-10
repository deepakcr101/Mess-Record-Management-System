package com.messmanagement.subscription.dto;

import com.messmanagement.subscription.entity.SubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponseDTO {
    private Long subscriptionId;
    private Long userId; // Or UserResponseDTO if you want more user details
    private String userEmail; // For convenience
    private LocalDate startDate;
    private LocalDate endDate;
    private SubscriptionStatus status;
    private BigDecimal amountPaid;
    private String paymentTransactionId;
    private String stripeSubscriptionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}