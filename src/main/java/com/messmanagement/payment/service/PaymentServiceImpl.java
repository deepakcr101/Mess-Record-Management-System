package com.messmanagement.payment.service;

import com.messmanagement.subscription.entity.Subscription;
import com.messmanagement.user.entity.User;
import com.stripe.Stripe; // Ensure Stripe is initialized (e.g., via StripeConfig)
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    @Value("${stripe.price-id.monthly-subscription}")
    private String monthlySubscriptionPriceId; // e.g., price_xxxxxxxxxxxxxx from Stripe Dashboard

    @Value("${app.frontend.url.base:http://localhost:3001}") // Base URL of your frontend
    private String frontendBaseUrl;
    
    @Value("${app.frontend.url.subscription.success:/payment/success?session_id={CHECKOUT_SESSION_ID}}")
    private String subscriptionSuccessUrlPath;

    @Value("${app.frontend.url.subscription.cancel:/payment/cancel}")
    private String subscriptionCancelUrlPath;


    @Override
    public String createSubscriptionCheckoutSession(User user, Subscription pendingSubscription, BigDecimal amount, String currency) throws StripeException {
        // Note: Stripe.apiKey should already be set globally via StripeConfig

        // 0. Potentially retrieve or create a Stripe Customer for the user
        // This ensures that subscriptions and payments are linked to a customer in Stripe.
        // You might want to store the stripe_customer_id on your User entity.
        String stripeCustomerId = getOrCreateStripeCustomer(user);


        // 1. Define Line Item for the Checkout Session (using the Price ID from Stripe Dashboard)
        SessionCreateParams.LineItem.PriceData.Recurring recurring = SessionCreateParams.LineItem.PriceData.Recurring.builder()
            .setInterval(SessionCreateParams.LineItem.PriceData.Recurring.Interval.MONTH)
            .build();
            
        // If using a fixed price ID directly, this is simpler:
        SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                .setPrice(monthlySubscriptionPriceId) // Use the Price ID for Rs. 3500/month
                .setQuantity(1L)
                .build();
        
        // 2. Define Success and Cancel URLs (these will be frontend URLs)
        // The frontend will handle displaying success/failure messages.
        String successUrl = frontendBaseUrl + subscriptionSuccessUrlPath; // e.g., http://localhost:3001/payment/success?session_id={CHECKOUT_SESSION_ID}
        String cancelUrl = frontendBaseUrl + subscriptionCancelUrlPath;   // e.g., http://localhost:3001/payment/cancel

        // 3. Create Session Parameters
        SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD) // Or other payment method types
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addLineItem(lineItem)
                .setCustomer(stripeCustomerId); // Associate with the Stripe customer

        // Include metadata to link Stripe session back to your internal subscription/user
        // This is VERY important for webhook handling later.
        paramsBuilder.putMetadata("app_user_id", user.getUserId().toString());
        paramsBuilder.putMetadata("app_subscription_id", pendingSubscription.getSubscriptionId().toString());
        // You could also include pendingSubscription.getAmountPaid().toPlainString() if needed for reconciliation

        // For subscriptions, Stripe might automatically handle trial periods or setup fees if configured on the Price.
        // If you need to pass subscription data directly (e.g., for trials not on the Price):
        // SessionCreateParams.SubscriptionData subscriptionData = SessionCreateParams.SubscriptionData.builder()
        //         // .setTrialPeriodDays(7L) // Example: 7-day trial
        //         .putMetadata("app_subscription_id", pendingSubscription.getSubscriptionId().toString())
        //         .build();
        // paramsBuilder.setSubscriptionData(subscriptionData);


        // 4. Create the Stripe Checkout Session
        Session session = Session.create(paramsBuilder.build());

        // 5. Return the Session ID (the frontend will use this to redirect to Stripe)
        return session.getId();
    }

    private String getOrCreateStripeCustomer(User user) throws StripeException {
        // This is a simplified example. In a real app, you'd likely store
        // the Stripe Customer ID on your User entity to avoid creating duplicates.

        // if (user.getStripeCustomerId() != null) {
        //     // Optional: Verify customer exists in Stripe, handle if not
        //     try {
        //         Customer stripeCustomer = Customer.retrieve(user.getStripeCustomerId());
        //         return stripeCustomer.getId();
        //     } catch (StripeException e) {
        //         // Handle error, maybe customer was deleted in Stripe?
        //         // For now, proceed to create
        //     }
        // }

        CustomerCreateParams customerParams = CustomerCreateParams.builder()
                .setName(user.getName())
                .setEmail(user.getEmail())
                .putMetadata("app_user_id", user.getUserId().toString())
                .build();
        Customer customer = Customer.create(customerParams);

        // TODO: Save customer.getId() to your User entity in your database
        // user.setStripeCustomerId(customer.getId());
        // userRepository.save(user);

        return customer.getId();
    }
}