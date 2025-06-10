package com.messmanagement.mealentry.dto;

import com.messmanagement.menu.entity.MealType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MealEntryResponseDTO {
    private Long entryId;
    private Long userId;
    private String userEmail; // For context
    private String userName;  // For context
    private MealType mealType;
    private LocalDate entryDate;
    private LocalTime entryTime;
    private Long verifiedByAdminId; // Optional
    private String verifiedByAdminName; // Optional, for display
}