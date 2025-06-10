package com.messmanagement.purchase.service;

import com.messmanagement.common.exception.ResourceNotFoundException;
import com.messmanagement.menu.entity.MenuItem;
import com.messmanagement.menu.repository.MenuItemRepository;
import com.messmanagement.menu.dto.MenuItemDTO; // For mapping
import com.messmanagement.payment.service.PaymentService; // To be created/used
import com.messmanagement.purchase.dto.DishPurchaseRequestDTO;
import com.messmanagement.purchase.dto.PurchaseResponseDTO;
import com.messmanagement.purchase.entity.Purchase;
import com.messmanagement.purchase.repository.PurchaseRepository;
import com.messmanagement.user.entity.User;
import com.messmanagement.user.repository.UserRepository;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final UserRepository userRepository;
    private final MenuItemRepository menuItemRepository;
    private final PaymentService paymentService; // Will be injected

    // Helper to map MenuItem entity to MenuItemDTO
    private MenuItemDTO mapMenuItemToDTO(MenuItem menuItem) {
        if (menuItem == null) return null;
        return new MenuItemDTO(
                menuItem.getItemId(),
                menuItem.getName(),
                menuItem.getDescription(),
                menuItem.getPrice(),
                menuItem.getCategory(),
                menuItem.getImageUrl(),
                menuItem.isAvailable(),
                menuItem.getCreatedAt(),
                menuItem.getUpdatedAt()
        );
    }

    // Helper to map Purchase entity to PurchaseResponseDTO
    private PurchaseResponseDTO mapPurchaseToDTO(Purchase purchase) {
        if (purchase == null) return null;
        return new PurchaseResponseDTO(
                purchase.getPurchaseId(),
                purchase.getUser().getUserId(),
                purchase.getUser().getEmail(),
                mapMenuItemToDTO(purchase.getMenuItem()), // Map nested MenuItem
                purchase.getQuantity(),
                purchase.getTotalAmount(),
                purchase.getPurchaseDate(),
                purchase.getPaymentTransactionId()
        );
    }

    @Override
    @Transactional
    public String initiateDishPurchase(String userEmail, DishPurchaseRequestDTO purchaseRequest) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        MenuItem menuItem = menuItemRepository.findById(purchaseRequest.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Menu Item not found with id: " + purchaseRequest.getItemId()));

        if (!menuItem.isAvailable()) {
            throw new IllegalArgumentException("Menu item '" + menuItem.getName() + "' is currently not available for purchase.");
        }

        BigDecimal totalAmount = menuItem.getPrice().multiply(BigDecimal.valueOf(purchaseRequest.getQuantity()));

        // Create a local Purchase record with a PENDING status (or similar initial status)
        Purchase purchase = new Purchase();
        purchase.setUser(user);
        purchase.setMenuItem(menuItem);
        purchase.setQuantity(purchaseRequest.getQuantity());
        purchase.setTotalAmount(totalAmount);
        // purchaseDate is set by @CreationTimestamp
        // paymentTransactionId will be set after successful payment

        Purchase pendingPurchase = purchaseRepository.save(purchase);

        // TODO: Interact with PaymentService to create a Stripe Checkout Session for ONE-TIME payment
        // This will be different from the subscription session.
        try {
            // Example: paymentService might need different parameters for one-time payment
            // String stripeSessionId = paymentService.createOneTimePaymentCheckoutSession(
            //         user,
            //         pendingPurchase, // Pass the pending purchase for metadata
            //         menuItem.getName(), // Description for Stripe line item
            //         totalAmount,
            //         "inr" // Currency
            // );
            // return stripeSessionId;

            // For now, placeholder:
            String placeholderStripeSessionId = "stripe_checkout_session_id_for_purchase_" + pendingPurchase.getPurchaseId();
            return placeholderStripeSessionId;

        } catch (/* StripeException e */ Exception e) { // Catch specific StripeException later
            // Log error
            // Rollback or delete pendingPurchase if payment initiation fails severely
            // For now, rethrow as runtime
            throw new RuntimeException("Payment gateway error during dish purchase: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PurchaseResponseDTO> getMyPurchaseHistory(String userEmail, Pageable pageable) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));
        Page<Purchase> purchases = purchaseRepository.findByUserOrderByPurchaseDateDesc(user, pageable);
        return purchases.map(this::mapPurchaseToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PurchaseResponseDTO> getAllPurchases(Pageable pageable) {
        // TODO: Add filtering for admin if needed (e.g., by user, date range)
        return purchaseRepository.findAll(pageable).map(this::mapPurchaseToDTO);
    }

    @Override
    @Transactional
    public PurchaseResponseDTO confirmDishPurchase(Long purchaseId, String paymentTransactionId) {
        Purchase purchase = purchaseRepository.findById(purchaseId)
            .orElseThrow(() -> new ResourceNotFoundException("Purchase record not found with id: " + purchaseId));

        // Here, we'd typically also verify the amount if possible or that the payment corresponds to this purchase.
        // For now, we assume the webhook handler has validated the event and provides the correct purchaseId.
        
        purchase.setPaymentTransactionId(paymentTransactionId);
        // Update purchase status if you have one (e.g., from PENDING to COMPLETED/PAID)
        // purchase.setStatus(PurchaseStatus.COMPLETED); 
        
        Purchase confirmedPurchase = purchaseRepository.save(purchase);
        return mapPurchaseToDTO(confirmedPurchase);
    }
}