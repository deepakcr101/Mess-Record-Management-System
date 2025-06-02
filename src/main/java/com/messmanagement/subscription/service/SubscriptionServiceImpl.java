package com.messmanagement.subscription.service;

import com.messmanagement.common.exception.ResourceNotFoundException;
import com.messmanagement.subscription.dto.SubscriptionPurchaseRequestDTO;
import com.messmanagement.subscription.dto.SubscriptionResponseDTO;
import com.messmanagement.subscription.entity.Subscription;
import com.messmanagement.subscription.entity.SubscriptionStatus;
import com.messmanagement.subscription.repository.SubscriptionRepository;
import com.messmanagement.user.entity.User;
import com.messmanagement.user.repository.UserRepository;
import com.messmanagement.payment.service.PaymentService;
import com.stripe.exception.StripeException;
// We'll need PaymentService later
// import com.messmanagement.payment.service.PaymentService;
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
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

     private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final PaymentService paymentService; 

    @Value("${mess.subscription.monthly.price:3500.00}") // Default to 3500, from plan
    private BigDecimal monthlySubscriptionPrice;

    @Value("${mess.subscription.duration.months:1}") // Duration in months
    private int subscriptionDurationMonths;

     // Update constructor
    public SubscriptionServiceImpl(SubscriptionRepository subscriptionRepository,
                                 UserRepository userRepository,
                                 PaymentService paymentService) { // Add PaymentService
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
        this.paymentService = paymentService; // Assign
    }

    // Helper to map Entity to DTO ... (remains the same)
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


    @Override
    @Transactional
    public String purchaseSubscription(String userEmail, SubscriptionPurchaseRequestDTO purchaseRequest) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Optional<Subscription> existingActiveSubscription = subscriptionRepository
                .findByUserAndStatusIn(user, Arrays.asList(SubscriptionStatus.ACTIVE, SubscriptionStatus.PENDING_PAYMENT));

        if (existingActiveSubscription.isPresent()) {
            Subscription sub = existingActiveSubscription.get();
            if (sub.getStatus() == SubscriptionStatus.ACTIVE) {
                throw new IllegalStateException("User already has an active subscription ending on " + sub.getEndDate());
            } else if (sub.getStatus() == SubscriptionStatus.PENDING_PAYMENT) {
                // Potentially retrieve and return existing Stripe session ID if still valid
                // For now, simplified:
                throw new IllegalStateException("A subscription payment is already pending. Please complete it or wait.");
            }
        }

        LocalDate startDate = LocalDate.now(); // Or determine based on existing sub if any
        LocalDate endDate = startDate.plusMonths(subscriptionDurationMonths);

        Subscription newSubscription = new Subscription();
        newSubscription.setUser(user);
        newSubscription.setStartDate(startDate); 
        newSubscription.setEndDate(endDate);
        newSubscription.setStatus(SubscriptionStatus.PENDING_PAYMENT);
        newSubscription.setAmountPaid(monthlySubscriptionPrice); // Expected amount

        Subscription pendingSubscription = subscriptionRepository.save(newSubscription);

        try {
            // Use PaymentService to create Stripe Checkout Session
            String stripeSessionId = paymentService.createSubscriptionCheckoutSession(
                    user,
                    pendingSubscription,
                    monthlySubscriptionPrice,
                    "inr" // Assuming currency is INR
            );
            // The pendingSubscription object now has an ID.
            // We passed it to createSubscriptionCheckoutSession for metadata.
            return stripeSessionId;
        } catch (StripeException e) {
            // Log the StripeException
            // e.g., logger.error("Stripe error while creating checkout session for user {}: {}", userEmail, e.getMessage());
            // Rollback or handle the pendingSubscription state if needed, though @Transactional should help.
            // For now, rethrow as a runtime exception or a custom payment exception.
            throw new RuntimeException("Payment gateway error: " + e.getMessage(), e);
        }
    }

    // ... (getCurrentSubscriptionStatus, getAllSubscriptions, activateSubscription, cancelSubscription methods remain for now) ...
    // Note: activateSubscription will be critical for webhooks.
    // cancelSubscription will need to call paymentService.cancelStripeSubscription(stripeSubscriptionId)
    @Override
    @Transactional(readOnly = true)
    public SubscriptionResponseDTO getCurrentSubscriptionStatus(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));
        return subscriptionRepository.findFirstByUserOrderByEndDateDesc(user)
                .map(this::mapToDTO)
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
        Subscription subscription = subscriptionRepository.findByStripeSubscriptionId(stripeSubscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription to activate not found for stripeSubscriptionId: " + stripeSubscriptionId));

        if (subscription.getStatus() == SubscriptionStatus.PENDING_PAYMENT || subscription.getStatus() == SubscriptionStatus.ACTIVE) { 
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscription.setPaymentTransactionId(paymentTransactionId); 
            subscription.setAmountPaid(amountPaid); 
            subscription.setStartDate(startDate); 
            subscription.setEndDate(endDate);     

            User user = subscription.getUser();
            // Example of setting stripe customer id on user if you decide to store it
            // if(user.getStripeCustomerId() == null && stripeCustomerId != null){
            //    user.setStripeCustomerId(stripeCustomerId);
            //    userRepository.save(user);
            // }
            subscriptionRepository.save(subscription);
        } else {
             // logger.warn("Attempted to activate a subscription that is not PENDING_PAYMENT. Current status: " + subscription.getStatus());
        }
    }

    @Override
    @Transactional
    public SubscriptionResponseDTO cancelSubscription(Long subscriptionId, String cancelledByUserEmail) {
        User user = userRepository.findByEmail(cancelledByUserEmail)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + cancelledByUserEmail));

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
            .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + subscriptionId));

        if (!subscription.getUser().equals(user) /* && !user.getRole().equals(Role.ADMIN) */) {
            throw new SecurityException("User not authorized to cancel this subscription.");
        }

        if (subscription.getStatus() == SubscriptionStatus.ACTIVE) {
            // TODO: try { paymentService.cancelStripeSubscription(subscription.getStripeSubscriptionId()); } catch (StripeException e) { // handle }
            subscription.setStatus(SubscriptionStatus.CANCELLED);
            // subscription.setEndDate(LocalDate.now()); // Or end of billing period
            Subscription cancelledSubscription = subscriptionRepository.save(subscription);
            return mapToDTO(cancelledSubscription);
        } else {
            throw new IllegalStateException("Subscription cannot be cancelled as it's not active. Current status: " + subscription.getStatus());
        }
    }
}