package com.messmanagement.purchase.controller;

import com.messmanagement.purchase.dto.DishPurchaseRequestDTO;
import com.messmanagement.purchase.dto.PurchaseResponseDTO;
import com.messmanagement.purchase.service.PurchaseService;
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
@RequestMapping("/api/v1/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    /**
     * Student buys an individual dish.
     * POST /api/v1/purchases
     * Payload: DishPurchaseRequestDTO
     * Response: String (Stripe Checkout Session ID or similar for payment initiation)
     * Secured for STUDENT.
     */
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<String> purchaseDish(
            Authentication authentication,
            @Valid @RequestBody DishPurchaseRequestDTO purchaseRequest) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String userEmail = userDetails.getUsername();

        String paymentInitiationResponse = purchaseService.initiateDishPurchase(userEmail, purchaseRequest);
        // This response will be a Stripe session ID/URL to redirect the client for payment.
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentInitiationResponse);
    }

    /**
     * Student views their purchase history.
     * GET /api/v1/purchases/my-history
     * Response: Page<PurchaseResponseDTO>
     * Secured for STUDENT.
     */
    @GetMapping("/my-history")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Page<PurchaseResponseDTO>> getMyPurchaseHistory(
            Authentication authentication,
            @PageableDefault(size = 10, sort = "purchaseDate") Pageable pageable) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String userEmail = userDetails.getUsername();

        Page<PurchaseResponseDTO> purchaseHistory = purchaseService.getMyPurchaseHistory(userEmail, pageable);
        return ResponseEntity.ok(purchaseHistory);
    }

    /**
     * Admin views all individual purchases with filtering and pagination.
     * GET /api/v1/purchases
     * Response: Page<PurchaseResponseDTO>
     * Secured for ADMIN.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<PurchaseResponseDTO>> getAllPurchases(
            @PageableDefault(size = 10, sort = "purchaseDate") Pageable pageable) {
        // TODO: Add request parameters for filtering if needed by admin, and pass to service
        Page<PurchaseResponseDTO> purchases = purchaseService.getAllPurchases(pageable);
        return ResponseEntity.ok(purchases);
    }
}