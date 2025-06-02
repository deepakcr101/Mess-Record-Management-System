package com.messmanagement.subscription.controller;

import com.messmanagement.subscription.dto.SubscriptionPurchaseRequestDTO;
import com.messmanagement.subscription.dto.SubscriptionResponseDTO;
import com.messmanagement.subscription.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    /**
     * Student purchases a monthly subscription.
     * POST /api/v1/subscriptions/purchase
     * Payload: SubscriptionPurchaseRequestDTO (can be minimal)
     * Response: String (e.g., Stripe Checkout Session ID or redirect URL)
     * Secured for STUDENT.
     */
    @PostMapping("/purchase")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<String> purchaseSubscription(
            Authentication authentication,
            @Valid @RequestBody(required = false) SubscriptionPurchaseRequestDTO purchaseRequest) {
        // (required = false) for RequestBody if DTO can be empty or defaults are handled by service

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String userEmail = userDetails.getUsername();

        // If purchaseRequest is null (e.g. empty body sent), create a default one or handle in service
        SubscriptionPurchaseRequestDTO request = (purchaseRequest == null) ? new SubscriptionPurchaseRequestDTO() : purchaseRequest;
        
        String paymentInitiationResponse = subscriptionService.purchaseSubscription(userEmail, request);
        // This response will be a Stripe session ID/URL to redirect the client for payment.
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentInitiationResponse);
    }

    /**
     * Student views their current subscription status and expiry date.
     * GET /api/v1/subscriptions/my-status
     * Response: SubscriptionResponseDTO
     * Secured for STUDENT.
     */
    @GetMapping("/my-status")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<SubscriptionResponseDTO> getMySubscriptionStatus(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String userEmail = userDetails.getUsername();
        SubscriptionResponseDTO subscriptionStatus = subscriptionService.getCurrentSubscriptionStatus(userEmail);
        if (subscriptionStatus == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Or a specific DTO indicating no active sub
        }
        return ResponseEntity.ok(subscriptionStatus);
    }

    /**
     * Admin views all subscriptions with filtering and pagination.
     * GET /api/v1/subscriptions
     * Response: Page<SubscriptionResponseDTO>
     * Secured for ADMIN.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<SubscriptionResponseDTO>> getAllSubscriptions(
            @PageableDefault(size = 10, sort = "startDate") Pageable pageable) {
        // TODO: Add request parameters for filtering if needed, and pass to service
        Page<SubscriptionResponseDTO> subscriptions = subscriptionService.getAllSubscriptions(pageable);
        return ResponseEntity.ok(subscriptions);
    }
    
    /**
     * Student/Admin cancels a subscription.
     * POST /api/v1/subscriptions/{subscriptionId}/cancel
     * Response: Updated SubscriptionResponseDTO
     * Secured for any authenticated user (service layer handles ownership/admin check).
     */
    @PostMapping("/{subscriptionId}/cancel")
    @PreAuthorize("isAuthenticated()") // Allows both student (for their own) and admin
    public ResponseEntity<SubscriptionResponseDTO> cancelSubscription(
            @PathVariable Long subscriptionId,
            Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String userEmail = userDetails.getUsername();
        
        SubscriptionResponseDTO cancelledSubscription = subscriptionService.cancelSubscription(subscriptionId, userEmail);
        return ResponseEntity.ok(cancelledSubscription);
    }
}