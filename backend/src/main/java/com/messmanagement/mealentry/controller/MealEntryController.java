package com.messmanagement.mealentry.controller;

import com.messmanagement.mealentry.dto.MealEntryRequestDTO;
import com.messmanagement.mealentry.dto.MealEntryResponseDTO;
import com.messmanagement.mealentry.service.MealEntryService;
import com.messmanagement.menu.entity.MealType; // For request param
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/meal-entries")
@RequiredArgsConstructor
public class MealEntryController {

    private final MealEntryService mealEntryService;

    /**
     * Student marks their meal entry.
     * POST /api/v1/meal-entries/mark
     * Payload: MealEntryRequestDTO (mess_provided_user_id, meal_type)
     * Response: MealEntryResponseDTO
     * Secured for STUDENT.
     */
    @PostMapping("/mark")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<MealEntryResponseDTO> markMealEntry(
            Authentication authentication,
            @Valid @RequestBody MealEntryRequestDTO requestDTO) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String authenticatedUserEmail = userDetails.getUsername();

        MealEntryResponseDTO createdEntry = mealEntryService.markMealEntry(authenticatedUserEmail, requestDTO);
        return new ResponseEntity<>(createdEntry, HttpStatus.CREATED);
    }

    /**
     * Student views their meal entry history.
     * GET /api/v1/meal-entries/my-history
     * Response: Page<MealEntryResponseDTO>
     * Secured for STUDENT.
     */
    @GetMapping("/my-history")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Page<MealEntryResponseDTO>> getMyMealEntryHistory(
            Authentication authentication,
            @PageableDefault(size = 10, sort = {"entryDate", "entryTime"}, direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String authenticatedUserEmail = userDetails.getUsername();

        Page<MealEntryResponseDTO> history = mealEntryService.getMyMealEntryHistory(authenticatedUserEmail, pageable);
        return ResponseEntity.ok(history);
    }

    /**
     * Admin views all meal entries with filtering.
     * GET /api/v1/meal-entries
     * Filters: entryDate (YYYY-MM-DD), userId, mealType
     * Response: Page<MealEntryResponseDTO>
     * Secured for ADMIN.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<MealEntryResponseDTO>> getAllMealEntriesForAdmin(
            @PageableDefault(size = 10, sort = {"entryDate", "entryTime"}, direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate entryDate,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) MealType mealType) {

        Page<MealEntryResponseDTO> entries = mealEntryService.getAllMealEntriesForAdmin(pageable, entryDate, userId, mealType);
        return ResponseEntity.ok(entries);
    }
}