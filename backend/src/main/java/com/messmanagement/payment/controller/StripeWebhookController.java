package com.messmanagement.payment.controller;

import com.messmanagement.subscription.service.SubscriptionService;
import com.messmanagement.purchase.service.PurchaseService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.model.Invoice;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@RestController
@RequestMapping("/api/v1/stripe/webhooks")
@RequiredArgsConstructor
public class StripeWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(StripeWebhookController.class);

    private final SubscriptionService subscriptionService;
    private final PurchaseService purchaseService;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @PostMapping
    public ResponseEntity<String> handleStripeEvent(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {

        if (sigHeader == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing Stripe-Signature header");
        }

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            logger.warn("Webhook error: Invalid signature. {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        } catch (Exception e) {
            logger.error("Webhook error while constructing event. {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook error");
        }
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
StripeObject stripeObject = null;
if (dataObjectDeserializer.getObject().isPresent()) {
stripeObject = dataObjectDeserializer.getObject().get();
} else {
logger.error("Webhook error: Deserialization of event data object failed for event ID {}", event.getId());
return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Webhook error: Deserialization failed.");
}

        logger.info("Received Stripe event: id={}, type={}", event.getId(), event.getType());

        // Handle the event
        switch (event.getType()) {
            case "checkout.session.completed":
                Session checkoutSession = (Session) stripeObject;
                handleCheckoutSessionCompleted(checkoutSession);
                break;
            case "invoice.paid":
                Invoice invoice = (Invoice) stripeObject;
                handleInvoicePaid(invoice);
                break;
            case "invoice.payment_failed":
                Invoice failedInvoice = (Invoice) stripeObject;
                handleInvoicePaymentFailed(failedInvoice);
                break;
            // Add other event types as needed, e.g., 'customer.subscription.deleted'
            default:
                logger.warn("Unhandled event type: {}", event.getType());
        }

        return ResponseEntity.ok("Webhook received");
    }

    private void handleCheckoutSessionCompleted(Session session) {
        logger.info("Handling checkout.session.completed for session ID: {}", session.getId());
        // This event is useful for linking our records with Stripe's records.
        // The actual service activation is better handled by 'invoice.paid'.
        
        String mode = session.getMode();
        if ("subscription".equals(mode)) {
            String stripeSubscriptionId = session.getSubscription();
            String localSubscriptionId = session.getMetadata().get("app_subscription_id");

            // Update your local subscription record (the one with PENDING status)
            // with the stripeSubscriptionId.
            if (localSubscriptionId != null && stripeSubscriptionId != null) {
                subscriptionService.linkStripeId(Long.parseLong(localSubscriptionId), stripeSubscriptionId);
                logger.info("Linked Stripe subscription ID {} to local subscription ID {}", stripeSubscriptionId, localSubscriptionId);
            } else {
                logger.warn("Missing localSubscriptionId or stripeSubscriptionId when handling checkout.session.completed");
            }
            logger.info("Subscription created in Stripe with ID: {}. Linked to local subscription ID: {}", stripeSubscriptionId, localSubscriptionId);

        } else if ("payment".equals(mode)) {
            String paymentIntentId = session.getPaymentIntent();
            String localPurchaseId = session.getMetadata().get("app_purchase_id");
            
            // For one-time payments, we can often confirm the purchase right away.
            // However, 'charge.succeeded' or 'payment_intent.succeeded' are more definitive confirmations of payment.
            // Let's assume for now we use 'invoice.paid' for subscriptions and this event for dish purchases if simple.
            purchaseService.confirmDishPurchase(Long.parseLong(localPurchaseId), paymentIntentId);
            logger.info("One-time payment confirmed for purchase ID: {}. Stripe Payment Intent ID: {}", localPurchaseId, paymentIntentId);
        }
    }

    private void handleInvoicePaid(Invoice invoice) {
        logger.info("Handling invoice.paid for invoice ID: {}", invoice.getId());
        // This event confirms a subscription payment was successful.
        // This is the best place to ACTIVATE the service.
        
        // "billing_reason": "subscription_create" for the first invoice of a new subscription.
        // "billing_reason": "subscription_cycle" for renewals.
        
        String stripeSubscriptionId = invoice.getSubscription();
        if (stripeSubscriptionId == null) {
            logger.warn("Invoice.paid event received with no subscription ID. Invoice ID: {}", invoice.getId());
            return;
        }

        String stripeCustomerId = invoice.getCustomer();
        String paymentTransactionId = invoice.getPaymentIntent();
        BigDecimal amountPaid = BigDecimal.valueOf(invoice.getAmountPaid()).movePointLeft(2); // Stripe amounts are in cents
        LocalDate startDate = Instant.ofEpochSecond(invoice.getPeriodStart()).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = Instant.ofEpochSecond(invoice.getPeriodEnd()).atZone(ZoneId.systemDefault()).toLocalDate();

        subscriptionService.activateSubscription(stripeSubscriptionId, stripeCustomerId, paymentTransactionId, amountPaid, startDate, endDate);
        logger.info("Subscription activated/renewed for Stripe Subscription ID: {}", stripeSubscriptionId);
        // Implement logic to handle failed payments.
        // e.g., send an email to the user, set subscription status to 'EXPIRED' or a special 'PAYMENT_FAILED' status.
        if (failedInvoice.getSubscription() != null) {
            subscriptionService.handleFailedPayment(failedInvoice.getSubscription());
            logger.info("Handled failed payment for subscription ID: {}", failedInvoice.getSubscription());
        } else {
            logger.warn("Failed invoice does not have a subscription ID: {}", failedInvoice.getId());
        }
        logger.warn("Handling invoice.payment_failed for invoice ID: {}", failedInvoice.getId());
        // TODO: Implement logic to handle failed payments.
        // e.g., send an email to the user, set subscription status to 'EXPIRED' or a special 'PAYMENT_FAILED' status.
        // subscriptionService.handleFailedPayment(failedInvoice.getSubscription());
    }
}