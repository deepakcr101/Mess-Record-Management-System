package com.messmanagement.subscription.service;

import com.messmanagement.common.exception.ResourceNotFoundException;
import com.messmanagement.payment.service.PaymentService; // Make sure this is the correct import
import com.messmanagement.subscription.dto.MySubscriptionStatusDTO; // IMPORT THE NEW DTO
import com.messmanagement.subscription.dto.SubscriptionPurchaseRequestDTO;
import com.messmanagement.subscription.dto.SubscriptionResponseDTO;

import com.messmanagement.subscription.entity.Subscription;
import com.messmanagement.subscription.entity.SubscriptionStatus;
import com.messmanagement.subscription.repository.SubscriptionRepository;
import com.messmanagement.user.entity.User;
import com.messmanagement.user.repository.UserRepository;
import com.stripe.exception.StripeException; // Keep for purchaseSubscription
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor // This Lombok annotation handles the constructor for final fields
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final PaymentService paymentService;

    @Value("${mess.subscription.monthly.price:3500.00}")
    private BigDecimal monthlySubscriptionPrice;

    @Value("${mess.subscription.duration.months:1}")
    private int subscriptionDurationMonths;

    // Your existing mapToDTO for SubscriptionResponseDTO
    private SubscriptionResponseDTO mapToDTO(Subscription subscription) {
        if (subscription == null) return null;
        return new SubscriptionResponseDTO(
                subscription.getSubscriptionId(),
                subscription.getUser().getUserId(),
                subscription.getUser().getEmail(),
                subscription.getStartDate(),
                subscription.getEndDate(),
                subscription.getStatus(),
                subscription.getAmountPaid(),
                subscription.getPaymentTransactionId(),
                subscription.getStripeSubscriptionId(),
                subscription.getCreatedAt(),
                subscription.getUpdatedAt()
        );
    }

    // --- NEW METHOD AND HELPER FOR MySubscriptionStatusDTO ---
    @Override
    @Transactional(readOnly = true)
    public MySubscriptionStatusDTO getDetailedUserSubscriptionStatus(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        // Fetch the subscription that ends/ended latest. This is usually the most relevant.
        Optional<Subscription> latestSubscriptionOpt = subscriptionRepository.findFirstByUserOrderByEndDateDesc(user);

        if (latestSubscriptionOpt.isEmpty()) {
            // If user has no subscriptions at all, return a DTO indicating this,
            // matching the constructor signature:
            // MySubscriptionStatusDTO(Long subscriptionId, String stripeSubscriptionId, String status, ...)
            return new MySubscriptionStatusDTO(
                    null,                       // 1st arg: subscriptionId (Long)
                    null,                       // 2nd arg: stripeSubscriptionId (String)
                    "NO_SUBSCRIPTION_HISTORY",  // 3rd arg: status (String)
                    null,                       // 4th arg: startDate (LocalDate)
                    null,                       // 5th arg: endDate (LocalDate)
                    null,                       // 6th arg: planName (String)
                    null,                       // 7th arg: amountPaid (BigDecimal)
                    null                        // 8th arg: createdAt (LocalDateTime)
            );
        }
        
        return mapToMySubscriptionStatusDTO(latestSubscriptionOpt.get());
    }

    private MySubscriptionStatusDTO mapToMySubscriptionStatusDTO(Subscription subscription) {
        // This method directly maps the Subscription entity to the MySubscriptionStatusDTO
        // It's assumed that the 'status' field on the Subscription entity is kept up-to-date
        // by your application logic (e.g., after Stripe webhooks for payment success/failure, cancellation).

        String displayStatus = subscription.getStatus().name(); // e.g., "ACTIVE", "EXPIRED", "CANCELLED"

        // Optional: You can add logic here to derive a more user-friendly status if needed.
        // For example, if the DB status is ACTIVE but the endDate has passed, you might want to override:
        if (subscription.getStatus() == SubscriptionStatus.ACTIVE &&
            subscription.getEndDate() != null &&
            subscription.getEndDate().isBefore(LocalDate.now())) {
            displayStatus = "EXPIRED"; // Or whatever term your frontend expects
        }

        // --- Plan Name Logic ---
        // How you get planName depends on your system design:
        // 1. If you store 'planName' or a 'planId' directly on your Subscription entity:
        //    String planName = subscription.getPlanNameFromEntity(); // Example
        // 2. If you need to derive it based on amount or Stripe ID patterns (less reliable):
        //    String planName = determinePlanNameBasedOnAmount(subscription.getAmountPaid());
        // 3. If you need to call Stripe API to get Product name from Price ID (adds latency):
        //    This would require injecting StripeService and making a call.
        //    For now, let's use a placeholder or a value from your configuration if it's fixed.
        String planName;
        if (subscription.getAmountPaid() != null && monthlySubscriptionPrice.compareTo(subscription.getAmountPaid()) == 0) {
            planName = "Monthly Mess Subscription"; // Example derivation
        } else {
            planName = "Custom Subscription"; // Or "Unknown Plan" or fetch from Stripe
        }
        // Ideally, you'd store the plan's name or a reference to it when the subscription is created.

        return new MySubscriptionStatusDTO(
                subscription.getSubscriptionId(), // This is Long
                subscription.getStripeSubscriptionId(), // This is String
                displayStatus, // String
                subscription.getStartDate(), // LocalDate
                subscription.getEndDate(), // LocalDate
                planName, // String
                subscription.getAmountPaid(), // BigDecimal
                subscription.getCreatedAt() // LocalDateTime
        );
    }
    // --- END OF NEW METHOD AND HELPER ---


    @Override
    @Transactional
    public String purchaseSubscription(String userEmail, SubscriptionPurchaseRequestDTO purchaseRequest) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        // Check for existing active or pending subscriptions
        // Consider fetching only PENDING_PAYMENT or truly ACTIVE (end_date in future)
        Optional<Subscription> existingSubscriptionOpt = subscriptionRepository
                .findByUserAndStatusIn(user, Arrays.asList(SubscriptionStatus.ACTIVE, SubscriptionStatus.PENDING_PAYMENT));
        
        if (existingSubscriptionOpt.isPresent()) {
            Subscription sub = existingSubscriptionOpt.get();
            // Only prevent new purchase if truly active and not past end date, or still pending
            if (sub.getStatus() == SubscriptionStatus.ACTIVE && (sub.getEndDate() == null || !sub.getEndDate().isBefore(LocalDate.now()))) {
                 throw new IllegalStateException("User already has an active subscription ending on " + sub.getEndDate() + ". Cannot purchase a new one yet.");
            } else if (sub.getStatus() == SubscriptionStatus.PENDING_PAYMENT) {
                // TODO: Consider if you should try to retrieve and return the existing Stripe Checkout Session ID
                // if it's still valid and for the same plan details. This is more complex.
                // For now, prevent new checkout if one is already pending.
                // paymentService.retrieveCheckoutSession(sub.getStripeCheckoutSessionId()); // Example if you store and retrieve
                throw new IllegalStateException("A subscription payment is already pending. Please complete or cancel the existing payment process.");
            }
            // If ACTIVE but past end_date, it's effectively EXPIRED, allow new purchase.
        }


        // Determine start and end dates for the new subscription
        LocalDate startDate = LocalDate.now();
        // If user had a previous subscription, you might want to align startDate
        // For simplicity, new subscriptions start today.
        LocalDate endDate = startDate.plusMonths(subscriptionDurationMonths);

        // For now, we assume a fixed monthly subscription price and duration from properties
        // If purchaseRequestDTO contains a planId/priceId, use that instead.
        BigDecimal actualAmountForThisPlan = monthlySubscriptionPrice; 
        // String stripePriceId = purchaseRequest.getStripePriceId(); // This is what you should use with Stripe

        Subscription newSubscription = new Subscription();
        newSubscription.setUser(user);
        newSubscription.setStartDate(startDate);
        newSubscription.setEndDate(endDate);
        newSubscription.setStatus(SubscriptionStatus.PENDING_PAYMENT);
        newSubscription.setAmountPaid(actualAmountForThisPlan); // This is the expected amount
        // newSubscription.setStripePriceId(stripePriceId); // Store the Stripe Price ID used!
        // newSubscription.setPlanName("Monthly Plan"); // Store the plan name from DTO or Stripe Product!


        // Save the PENDING_PAYMENT subscription first to get an ID if your payment service needs it
        Subscription pendingSubscription = subscriptionRepository.save(newSubscription);

        try {
            String stripeCheckoutSessionId = paymentService.createSubscriptionCheckoutSession(
                    user,
                    pendingSubscription, // Pass the entity with its ID
                    purchaseRequest.getStripePriceId(), // THIS IS THE CRUCIAL STRIPE PRICE ID from the frontend/request
                    "inr" // Or get currency from request/config
            );
            
            // Optionally, save the stripeCheckoutSessionId to the pendingSubscription if needed later
            // pendingSubscription.setStripeCheckoutSessionId(stripeCheckoutSessionId);
            // subscriptionRepository.save(pendingSubscription);

            return stripeCheckoutSessionId; // Return the Stripe Checkout Session ID to the frontend
        } catch (StripeException e) {
            // Important: Log the detailed Stripe error
            // logger.error("Stripe error for user {}: Code: {}, Message: {}, RequestID: {}", userEmail, e.getCode(), e.getMessage(), e.getRequestId(), e);
            // The @Transactional annotation should handle rollback of the pendingSubscription if an error occurs here.
            throw new RuntimeException("Payment gateway error: " + e.getMessage() + "; code: " + e.getCode() + "; request-id: " + e.getRequestId(), e);
        }
    }


    @Override
    @Transactional(readOnly = true)
    public SubscriptionResponseDTO getCurrentSubscriptionStatus(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));
        return subscriptionRepository.findFirstByUserOrderByEndDateDesc(user)
                .map(this::mapToDTO) // Uses your original mapToDTO
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SubscriptionResponseDTO> getAllSubscriptions(Pageable pageable) {
        return subscriptionRepository.findAll(pageable).map(this::mapToDTO);
    }

    @Override
    @Transactional
    public void activateSubscription(String stripeSubscriptionId, String stripeCustomerId, String paymentTransactionId, BigDecimal amountPaid, LocalDate startDate, LocalDate endDate) {
        // Find by Stripe Subscription ID, as this is what the webhook will provide reliably.
        Subscription subscription = subscriptionRepository.findByStripeSubscriptionId(stripeSubscriptionId)
                .orElseGet(() -> {
                    // If not found by stripeSubscriptionId (e.g., it's a new subscription not yet linked),
                    // try to find a PENDING_PAYMENT subscription for the customer that could match.
                    // This part is more complex and depends on what data Stripe webhook provides for first payment.
                    // For simplicity, we might assume checkout session ID was used to find/update the pending sub.
                    // If your webhook directly gives you your internal subscription ID via metadata, that's even better.
                    // For now, we rely on stripeSubscriptionId being set on the PENDING sub, or the webhook updating it.
                    // Let's assume the webhook will provide enough data to find the PENDING subscription
                    // or that the stripeSubscriptionId is already set on the pending record by your checkout success handler.
                    // A more robust way: Pass your internal pendingSubscription.getSubscriptionId() as metadata to Stripe Checkout,
                    // and retrieve it from the webhook event.
                    // For now, if not found by stripeSubscriptionId, we throw. This means stripeSubscriptionId should be
                    // populated on the Subscription entity BEFORE or DURING this activation.
                    // Often, the first payment_intent.succeeded or checkout.session.completed gives you the stripe_subscription_id.
                    throw new ResourceNotFoundException("Subscription to activate not found for stripeSubscriptionId: " + stripeSubscriptionId + ". Ensure stripeSubscriptionId is set on the pending subscription.");
                });

        // Update user's stripe_customer_id if not already set
        User user = subscription.getUser();
        // if (user.getStripeCustomerId() == null && stripeCustomerId != null) {
        //     user.setStripeCustomerId(stripeCustomerId);
        //     userRepository.save(user);
        // }

        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setPaymentTransactionId(paymentTransactionId); // This is likely the Stripe Charge ID or Payment Intent ID
        subscription.setAmountPaid(amountPaid); // Confirm this amount matches
        subscription.setStripeSubscriptionId(stripeSubscriptionId); // Ensure this is set
        
        // Stripe often dictates the actual start/end of the billing period
        subscription.setStartDate(startDate);
        subscription.setEndDate(endDate);
        
        subscriptionRepository.save(subscription);
    }

    @Override
    @Transactional
    public SubscriptionResponseDTO cancelSubscription(Long internalSubscriptionId, String cancellerUserEmail) {
        User canceller = userRepository.findByEmail(cancellerUserEmail)
            .orElseThrow(() -> new ResourceNotFoundException("User (canceller) not found: " + cancellerUserEmail));

        Subscription subscription = subscriptionRepository.findById(internalSubscriptionId)
            .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + internalSubscriptionId));

        // Security check: User can cancel their own, or an Admin can cancel any.
        // if (!subscription.getUser().equals(canceller) && !canceller.getRole().equals(Role.ADMIN)) { // Assuming Role enum
        //     throw new SecurityException("User not authorized to cancel this subscription.");
        // }

        if (subscription.getStatus() == SubscriptionStatus.ACTIVE) {
            try {
                // Attempt to cancel with Stripe.
                // The second parameter to cancelStripeSubscription might be options like 'invoice_now' or 'prorate'.
                // Stripe's cancel typically sets it to cancel at the end of the current billing period by default.
                paymentService.cancelStripeSubscription(subscription.getStripeSubscriptionId() /*, cancellationOptions */);
                
                // Update your local status
                subscription.setStatus(SubscriptionStatus.CANCELLED); 
                // Note: Stripe might send a webhook for 'customer.subscription.updated' or 'deleted'
                // which would also update the status. Your webhook handler should be idempotent.
                // The endDate might remain the end of the current paid period.
                // Or you might set it to LocalDate.now() if cancellation is immediate.
                // This depends on your business logic and Stripe's cancellation behavior.
                // For "cancel at period end", endDate usually doesn't change here.

            } catch (StripeException e) {
                // logger.error("Stripe error while cancelling subscription {}: {}", subscription.getStripeSubscriptionId(), e.getMessage(), e);
                throw new RuntimeException("Payment gateway error during cancellation: " + e.getMessage(), e);
            }
            Subscription cancelledSubscription = subscriptionRepository.save(subscription);
            return mapToDTO(cancelledSubscription);
        } else {
            throw new IllegalStateException("Subscription cannot be cancelled as it's not active. Current status: " + subscription.getStatus());
        }
    }
}