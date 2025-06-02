package com.messmanagement.subscription.repository;

import com.messmanagement.subscription.entity.Subscription;
import com.messmanagement.subscription.entity.SubscriptionStatus;
import com.messmanagement.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long>, JpaSpecificationExecutor<Subscription> {

    // Find the current active or pending subscription for a user
    // Users should ideally have only one active/pending subscription at a time.
    Optional<Subscription> findByUserAndStatusIn(User user, List<SubscriptionStatus> statuses);

    // Find a user's most recent subscription (active or expired) to display status
    Optional<Subscription> findFirstByUserOrderByEndDateDesc(User user);

    // Find subscriptions by user for history or specific checks
    List<Subscription> findByUser(User user);

    // Find subscriptions by status (e.g., for admin to see all active subscriptions)
    Page<Subscription> findByStatus(SubscriptionStatus status, Pageable pageable);

    // Find subscriptions ending by a certain date (for expiry notifications - advanced feature)
    List<Subscription> findByEndDateBeforeAndStatus(LocalDate date, SubscriptionStatus status);
    
    // Find by Stripe Subscription ID (useful for webhook handling)
    Optional<Subscription> findByStripeSubscriptionId(String stripeSubscriptionId);

}
