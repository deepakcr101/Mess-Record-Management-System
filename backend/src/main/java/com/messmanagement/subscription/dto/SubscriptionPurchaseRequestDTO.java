package com.messmanagement.subscription.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank; // Optional: for validation

@Data
public class SubscriptionPurchaseRequestDTO {

    @NotBlank // Ensures the frontend sends this
    private String stripePriceId; // Frontend will send the actual Stripe Price ID, e.g., "price_xxxxxxxxxxxxxx"

    // You can remove the old planId field or keep it if it serves another internal purpose,
    // but stripePriceId should be the primary identifier for Stripe interactions.
}