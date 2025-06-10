package com.messmanagement.payment.service;

import com.messmanagement.subscription.entity.Subscription;
import com.messmanagement.user.entity.User;

// Stripe related imports
import com.stripe.Stripe; // Ensure Stripe is initialized (e.g., via a StripeConfig component or @PostConstruct)
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.checkout.Session; // Correct import for checkout.Session
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.SubscriptionUpdateParams; // Correct for updating subscriptions (cancellation)
import com.stripe.param.checkout.SessionCreateParams; // Correct import for checkout session params

import lombok.RequiredArgsConstructor; // Using this for constructor injection
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

// BigDecimal is not directly needed for creating session with Price ID
// import java.math.BigDecimal; 
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor // Injects final fields via constructor
public class PaymentServiceImpl implements PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);

    // This is your Stripe API Key (Secret Key)
    // It's better to initialize Stripe.apiKey once, e.g., in a @PostConstruct method or a config class
    @Value("${stripe.api.secret-key}")
    private String stripeSecretKey;

    // This should be the actual Stripe Price ID for your monthly plan
    // It seems you intend to pass the specific price ID from the request DTO now, which is better.
    // So, this field might become a default or one of many if you had multiple fixed plans configured here.
    // For now, let's assume the stripePriceId passed to createSubscriptionCheckoutSession is the one to use.
    // @Value("${stripe.price-id.monthly-subscription}")
    // private String defaultMonthlySubscriptionPriceId;

    @Value("${app.frontend.url.base:http://localhost:5173}") // Updated to 5173 as per your other examples
    private String frontendBaseUrl;
    
    @Value("${app.frontend.url.subscription.success:/payment/success?session_id={CHECKOUT_SESSION_ID}}")
    private String subscriptionSuccessUrlPath;

    @Value("${app.frontend.url.subscription.cancel:/payment/cancel}")
    private String subscriptionCancelUrlPath;

    // Initialize Stripe API key once when the service is created
    // @PostConstruct // Uncomment if you want to use @PostConstruct
    // public void init() {
    //     Stripe.apiKey = stripeSecretKey;
    //     logger.info("Stripe API Key Initialized.");
    // }
    // OR ensure Stripe.apiKey is set in a dedicated StripeConfig class.
    // For simplicity here, we can set it if not set, but a dedicated config is cleaner.
    private void ensureStripeApiKey() {
        if (Stripe.apiKey == null) {
            Stripe.apiKey = stripeSecretKey;
            logger.info("Stripe API Key Initialized in PaymentService.");
        }
    }


    @Override // Ensure this signature matches PaymentService interface
    public String createSubscriptionCheckoutSession(User user, 
                                                    Subscription pendingSubscription, 
                                                    String stripePriceId, // Changed from BigDecimal amount to String stripePriceId
                                                    String currency) throws StripeException {
        ensureStripeApiKey(); // Make sure API key is set

        String stripeCustomerId = getOrCreateStripeCustomer(user);

        // Validate stripePriceId - it should not be null or empty
        if (stripePriceId == null || stripePriceId.trim().isEmpty()) {
            logger.error("Stripe Price ID is null or empty for user: {}", user.getEmail());
            throw new IllegalArgumentException("Stripe Price ID cannot be null or empty.");
        }
        logger.info("Creating Stripe Checkout session for user: {}, priceId: {}", user.getEmail(), stripePriceId);


        // 1. Define Line Item for the Checkout Session using the provided Stripe Price ID
        SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                .setPrice(stripePriceId) // Use the Price ID passed from the purchase request
                .setQuantity(1L)
                .build();
        
        String successUrl = frontendBaseUrl + subscriptionSuccessUrlPath;
        String cancelUrl = frontendBaseUrl + subscriptionCancelUrlPath;

        SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addLineItem(lineItem)
                .setCustomer(stripeCustomerId);

        paramsBuilder.putMetadata("app_user_id", user.getUserId().toString());
        paramsBuilder.putMetadata("app_subscription_id", pendingSubscription.getSubscriptionId().toString());
        
        // Optional: Add subscription data for trials, etc., if not configured on the Price object in Stripe
        // SessionCreateParams.SubscriptionData subscriptionData = SessionCreateParams.SubscriptionData.builder()
        //         .putMetadata("app_subscription_id", pendingSubscription.getSubscriptionId().toString())
        //         // .setTrialPeriodDays(7L) // Example, if your price doesn't have a trial
        //         .build();
        // paramsBuilder.setSubscriptionData(subscriptionData);

        Session session = Session.create(paramsBuilder.build());
        logger.info("Stripe Checkout session created for user: {}, sessionId: {}", user.getEmail(), session.getId());
        return session.getId();
    }

    private String getOrCreateStripeCustomer(User user) throws StripeException {
        ensureStripeApiKey();
        // TODO: You should store and retrieve the user.getStripeCustomerId()
        // from your User entity to avoid creating duplicate Stripe customers.
        // For this example, we'll assume you might have a field on your User entity:
        // if (user.getStripeCustomerId() != null && !user.getStripeCustomerId().isBlank()) {
        //     try {
        //         Customer existingCustomer = Customer.retrieve(user.getStripeCustomerId());
        //         logger.info("Retrieved existing Stripe customer {} for user {}", existingCustomer.getId(), user.getEmail());
        //         return existingCustomer.getId();
        //     } catch (StripeException e) {
        //         logger.warn("Failed to retrieve existing Stripe customer {}: {}. Will create a new one.", user.getStripeCustomerId(), e.getMessage());
        //         // Fall through to create a new one if retrieve fails (e.g., customer deleted in Stripe)
        //     }
        // }

        CustomerCreateParams customerParams = CustomerCreateParams.builder()
                .setName(user.getName())
                .setEmail(user.getEmail())
                .putMetadata("app_user_id", user.getUserId().toString()) // Link Stripe customer to your app's user ID
                .build();
        Customer customer = Customer.create(customerParams);
        logger.info("Created new Stripe customer {} for user {}", customer.getId(), user.getEmail());

        // TODO: IMPORTANT - Persist customer.getId() to your User entity
        // user.setStripeCustomerId(customer.getId());
        // userRepository.save(user); // Assuming you inject UserRepository here or pass User back to calling service to save

        return customer.getId();
    }

    @Override // Ensure this signature matches PaymentService interface
    public void cancelStripeSubscription(String stripeSubscriptionId) throws StripeException {
        ensureStripeApiKey();
        logger.info("Attempting to cancel Stripe subscription: {}", stripeSubscriptionId);
        com.stripe.model.Subscription stripeSubscription = com.stripe.model.Subscription.retrieve(stripeSubscriptionId);
        
        SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
            .setCancelAtPeriodEnd(true) // Common practice: cancel at the end of the current billing period
            .build();
        stripeSubscription.update(params);
        logger.info("Stripe subscription {} marked to cancel at period end.", stripeSubscriptionId);
    }
}