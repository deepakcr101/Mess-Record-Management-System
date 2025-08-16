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

        // --- Start of Changes ---
        Event event; // Declare the event variable here
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            logger.warn("Webhook error: Invalid signature. {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        } catch (Exception e) {
            logger.error("Webhook error while constructing event. {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook error");
        }
        // --- End of Changes ---
        
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = null;
        if (dataObjectDeserializer.getObject().isPresent()) {
            stripeObject = dataObjectDeserializer.getObject().get();
        } else {
            logger.error("Webhook error: Deserialization of event data object failed for event ID {}", event.getId());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Webhook error: Deserialization failed.");
        }

        logger.info("Received Stripe event: id={}, type={}", event.getId(), event.getType());

        // The rest of the file (switch statement and handler methods) remains the same.
        // ...
        switch (event.getType()) {
            // ... cases
        }

        return ResponseEntity.ok("Webhook received");
    }



    private void handleCheckoutSessionCompleted(Session session) {
        // ... This method is correct as is
        logger.info("Handling checkout.session.completed for session ID: {}", session.getId());
        
        String mode = session.getMode();
        if ("subscription".equals(mode)) {
            String stripeSubscriptionId = session.getSubscription();
            String localSubscriptionId = session.getMetadata().get("app_subscription_id");

            if (localSubscriptionId != null && stripeSubscriptionId != null) {
                subscriptionService.linkStripeId(Long.parseLong(localSubscriptionId), stripeSubscriptionId);
                logger.info("Linked Stripe subscription ID {} to local subscription ID {}", stripeSubscriptionId, localSubscriptionId);
            } else {
                logger.warn("Missing localSubscriptionId or stripeSubscriptionId when handling checkout.session.completed");
            }
        } else if ("payment".equals(mode)) {
            String paymentIntentId = session.getPaymentIntent();
            String localPurchaseId = session.getMetadata().get("app_purchase_id");
            
            purchaseService.confirmDishPurchase(Long.parseLong(localPurchaseId), paymentIntentId);
            logger.info("One-time payment confirmed for purchase ID: {}. Stripe Payment Intent ID: {}", localPurchaseId, paymentIntentId);
        }
    }

    private void handleInvoicePaid(Invoice invoice) {
        logger.info("Handling invoice.paid for invoice ID: {}", invoice.getId());
        
        String stripeSubscriptionId = invoice.getSubscription();
        if (stripeSubscriptionId == null) {
            logger.warn("Invoice.paid event received with no subscription ID. Invoice ID: {}", invoice.getId());
            return;
        }

        String stripeCustomerId = invoice.getCustomer();
        String paymentTransactionId = invoice.getPaymentIntent();
        BigDecimal amountPaid = BigDecimal.valueOf(invoice.getAmountPaid()).movePointLeft(2);
        LocalDate startDate = Instant.ofEpochSecond(invoice.getPeriodStart()).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = Instant.ofEpochSecond(invoice.getPeriodEnd()).atZone(ZoneId.systemDefault()).toLocalDate();

        subscriptionService.activateSubscription(stripeSubscriptionId, stripeCustomerId, paymentTransactionId, amountPaid, startDate, endDate);
        logger.info("Subscription activated/renewed for Stripe Subscription ID: {}", stripeSubscriptionId);
    }

    // This is the new method you need to add
    private void handleInvoicePaymentFailed(Invoice failedInvoice) {
        logger.warn("Handling invoice.payment_failed for invoice ID: {}", failedInvoice.getId());
        
        if (failedInvoice.getSubscription() != null) {
            subscriptionService.handleFailedPayment(failedInvoice.getSubscription());
            logger.info("Handled failed payment for subscription ID: {}", failedInvoice.getSubscription());
        } else {
            logger.warn("Failed invoice does not have a subscription ID: {}", failedInvoice.getId());
        }
    }

   
}