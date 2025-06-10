package com.messmanagement.subscription.entity;

import com.messmanagement.user.entity.User; // Assuming User entity is in this package
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_id")
    private Long subscriptionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Foreign key to the User who subscribed [cite: 49]

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate; // [cite: 49]

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate; // [cite: 50]

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20) // Adjusted length for PENDING_PAYMENT
    private SubscriptionStatus status; // [cite: 50]

    @Column(name = "amount_paid", nullable = false, precision = 10, scale = 2)
    private BigDecimal amountPaid; // [cite: 51]

    @Column(name = "payment_transaction_id")
    private String paymentTransactionId; // [cite: 51]

    @Column(name = "stripe_subscription_id", unique = true) // Unique ID from Stripe [cite: 52]
    private String stripeSubscriptionId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}