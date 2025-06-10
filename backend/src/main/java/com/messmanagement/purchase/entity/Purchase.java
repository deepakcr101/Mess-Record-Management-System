package com.messmanagement.purchase.entity;

import com.messmanagement.menu.entity.MenuItem; // Import MenuItem
import com.messmanagement.user.entity.User;   // Import User
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime; // Using LocalDateTime for purchase_date as per plan

@Entity
@Table(name = "purchases")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "purchase_id")
    private Long purchaseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // The user who made the purchase

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private MenuItem menuItem; // The menu item that was purchased

    @Column(nullable = false)
    private Integer quantity = 1; // Default to 1, but can be more

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount; // Calculated based on item price and quantity

    @CreationTimestamp // Automatically set when the purchase is recorded
    @Column(name = "purchase_date", nullable = false, updatable = false)
    private LocalDateTime purchaseDate;

    @Column(name = "payment_transaction_id") // From the payment gateway for this specific purchase
    private String paymentTransactionId;

    // Add other fields if necessary, e.g., payment status if payment can be pending
}
