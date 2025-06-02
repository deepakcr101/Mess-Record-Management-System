package com.messmanagement.subscription.service;

import com.messmanagement.subscription.dto.SubscriptionPurchaseRequestDTO;
import com.messmanagement.subscription.dto.SubscriptionResponseDTO;
// Stripe related DTO or response for payment initiation might be needed later
// import com.stripe.model.checkout.Session; // Example if returning Stripe session directly

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;



public interface SubscriptionService {

    /**
     * Initiates the purchase of a monthly subscription for the given user.
     * This will interact with the PaymentService to create a Stripe Checkout session or PaymentIntent.
     * @param userEmail The email of the user purchasing the subscription.
     * @param purchaseRequest DTO containing any necessary details for purchase.
     * @return A DTO or object containing information for the client to proceed with payment (e.g., Stripe Checkout session ID/URL).
     * For now, let's define a placeholder response, maybe String for session ID or URL.
     */
    String purchaseSubscription(String userEmail, SubscriptionPurchaseRequestDTO purchaseRequest); // Placeholder return

    /**
     * Retrieves the current subscription status for the logged-in user.
     * @param userEmail The email of the user.
     * @return SubscriptionResponseDTO with the user's current/latest subscription details.
     */
    SubscriptionResponseDTO getCurrentSubscriptionStatus(String userEmail);

    /**
     * Retrieves all subscriptions with pagination and filtering (for Admin).
     * @param pageable Pagination information.
     * @return A page of SubscriptionResponseDTOs.
     */
    Page<SubscriptionResponseDTO> getAllSubscriptions(Pageable pageable /*, add filter DTO if needed */);
    
    /**
     * Handles successful payment notification, typically from a Stripe webhook.
     * @param stripeSubscriptionId Stripe's subscription ID.
     * @param stripeCustomerId Stripe's customer ID.
     * @param paymentTransactionId The payment transaction ID from Stripe.
     * @param amountPaid The actual amount paid.
     * @param startDate The start date of the subscription.
     * @param endDate The end date of the subscription.
     */
    void activateSubscription(String stripeSubscriptionId, String stripeCustomerId, String paymentTransactionId, BigDecimal amountPaid, LocalDate startDate, LocalDate endDate);

    /**
     * Handles subscription cancellation.
     * @param subscriptionId The ID of the subscription to cancel.
     * @param cancelledByUserEmail The email of the user initiating the cancellation (for auth check).
     * @return The updated SubscriptionResponseDTO.
     */
    SubscriptionResponseDTO cancelSubscription(Long subscriptionId, String cancelledByUserEmail);

    // More methods might be needed, e.g., to handle failed payments, subscription updates from Stripe webhooks, etc.
}