package com.messmanagement.purchase.service;

import com.messmanagement.purchase.dto.DishPurchaseRequestDTO;
import com.messmanagement.purchase.dto.PurchaseResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PurchaseService {

    /**
     * Initiates the purchase of an individual dish for the given user.
     * This will calculate the price, create a pending Purchase record,
     * and interact with PaymentService to get a Stripe Checkout Session ID or URL.
     * @param userEmail The email of the user making the purchase.
     * @param purchaseRequest DTO containing item ID and quantity.
     * @return A string representing the Stripe Checkout Session ID (or similar payment initiation info).
     */
    String initiateDishPurchase(String userEmail, DishPurchaseRequestDTO purchaseRequest);

    /**
     * Retrieves the purchase history for the logged-in user.
     * @param userEmail The email of the user.
     * @param pageable Pagination information.
     * @return A page of PurchaseResponseDTOs.
     */
    Page<PurchaseResponseDTO> getMyPurchaseHistory(String userEmail, Pageable pageable);

    /**
     * Retrieves all purchases with pagination (for Admin).
     * @param pageable Pagination information.
     * @return A page of PurchaseResponseDTOs.
     */
    Page<PurchaseResponseDTO> getAllPurchases(Pageable pageable /*, FilterDTO if needed */);
    
    /**
     * Confirms a dish purchase after successful payment.
     * This would typically be called from a webhook handler after Stripe confirms payment.
     * @param purchaseId The ID of the local purchase record created during initiation.
     * @param paymentTransactionId The transaction ID from Stripe.
     * @return The updated PurchaseResponseDTO.
     */
    PurchaseResponseDTO confirmDishPurchase(Long purchaseId, String paymentTransactionId);

    // Potentially methods to handle failed dish purchase payments
}