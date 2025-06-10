//src/main/java/com/messmanagement/subscription/dto/MySubscriptionStatusDTO.java
package com.messmanagement.subscription.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Add Lombok's @Data or individual getters/setters if you prefer
// import lombok.Data;
// @Data
public class MySubscriptionStatusDTO {

    private Long subscriptionId;
    private String stripeSubscriptionId;
    private String status; // e.g., "ACTIVE", "EXPIRED", "NO_SUBSCRIPTION_HISTORY"
    private LocalDate startDate;
    private LocalDate endDate;
    private String planName;
    private BigDecimal amountPaid;
    private LocalDateTime createdAt;

    // Default constructor (needed for libraries like Jackson)
    public MySubscriptionStatusDTO() {
    }

    // Constructor for "NO_SUBSCRIPTION_HISTORY" or simple status
    public MySubscriptionStatusDTO(String status) {
        this.status = status;
    }

    // Full constructor
    public MySubscriptionStatusDTO(Long subscriptionId, String stripeSubscriptionId, String status,
                                   LocalDate startDate, LocalDate endDate, String planName,
                                   BigDecimal amountPaid, LocalDateTime createdAt) {
        this.subscriptionId = subscriptionId;
        this.stripeSubscriptionId = stripeSubscriptionId;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.planName = planName;
        this.amountPaid = amountPaid;
        this.createdAt = createdAt;
    }

    // --- Add Getters and Setters for all fields if not using Lombok @Data ---
    public Long getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(Long subscriptionId) { this.subscriptionId = subscriptionId; }

    public String getStripeSubscriptionId() { return stripeSubscriptionId; }
    public void setStripeSubscriptionId(String stripeSubscriptionId) { this.stripeSubscriptionId = stripeSubscriptionId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }

    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}