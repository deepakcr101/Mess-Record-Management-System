package com.messmanagement.payment.service;

import com.messmanagement.subscription.entity.Subscription;
import com.messmanagement.user.entity.User;
import com.stripe.exception.StripeException; // Import StripeException

import java.math.BigDecimal;

public interface PaymentService {

    /**
     * Creates a Stripe Checkout Session for a new subscription.
     *
     * @param user The user for whom the subscription is being created.
     * @param pendingSubscription The local pending subscription record.
     * @param amount The amount for the subscription (e.g., 3500.00).
     * @param currency The currency code (e.g., "inr").
     * @return The ID of the created Stripe Checkout Session.
     * @throws StripeException if there's an error communicating with Stripe.
     */
    String createSubscriptionCheckoutSession(User user, Subscription pendingSubscription, BigDecimal amount, String currency) throws StripeException;

    // We will add methods for handling individual dish purchases later, e.g.:
    // String createOneTimePaymentCheckoutSession(User user, List<PurchaseItemDTO> items, String currency) throws StripeException;

    // Methods for handling webhooks might also be conceptually part of this service's scope,
    // or handled by a dedicated WebhookHandler service that uses PaymentService.
}