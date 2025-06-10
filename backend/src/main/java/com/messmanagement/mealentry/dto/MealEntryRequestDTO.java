package com.messmanagement.mealentry.dto;

import com.messmanagement.menu.entity.MealType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MealEntryRequestDTO {

    @NotBlank(message = "Mess Provided User ID cannot be blank.")
    private String messProvidedUserId; // For validation against the authenticated user

    @NotNull(message = "Meal type cannot be null.")
    private MealType mealType;
}