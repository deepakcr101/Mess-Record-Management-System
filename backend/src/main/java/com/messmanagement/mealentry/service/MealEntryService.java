package com.messmanagement.mealentry.service;

import com.messmanagement.mealentry.dto.MealEntryRequestDTO;
import com.messmanagement.mealentry.dto.MealEntryResponseDTO;
import com.messmanagement.menu.entity.MealType; // For admin filtering
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate; // For admin filtering

public interface MealEntryService {

    /**
     * Allows a student to mark their meal entry.
     * Validates the mess_provided_user_id against the authenticated user
     * and checks for an active subscription.
     *
     * @param authenticatedUserEmail The email of the currently logged-in student.
     * @param requestDTO DTO containing mess_provided_user_id and meal_type.
     * @return MealEntryResponseDTO of the created entry.
     */
    MealEntryResponseDTO markMealEntry(String authenticatedUserEmail, MealEntryRequestDTO requestDTO);

    /**
     * Retrieves the meal entry history for the logged-in student.
     *
     * @param authenticatedUserEmail The email of the student.
     * @param pageable Pagination information.
     * @return A page of MealEntryResponseDTOs.
     */
    Page<MealEntryResponseDTO> getMyMealEntryHistory(String authenticatedUserEmail, Pageable pageable);

    /**
     * Retrieves all meal entries for admin viewing, with filtering options.
     *
     * @param pageable Pagination information.
     * @param entryDate Optional filter by specific date.
     * @param userId Optional filter by user ID.
     * @param mealType Optional filter by meal type.
     * @return A page of MealEntryResponseDTOs.
     */
    Page<MealEntryResponseDTO> getAllMealEntriesForAdmin(Pageable pageable, LocalDate entryDate, Long userId, MealType mealType);
}