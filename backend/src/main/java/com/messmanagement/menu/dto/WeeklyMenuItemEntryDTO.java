package com.messmanagement.menu.dto;

import com.messmanagement.menu.entity.MealType;
import lombok.Data;
// import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalDate;

@Data
public class WeeklyMenuItemEntryDTO {
    // @NotNull
    private Long itemId;

    // @NotNull
    private DayOfWeek dayOfWeek;

    // @NotNull
    private MealType mealType;

    // @NotNull
    private LocalDate effectiveDateStart;

    private LocalDate effectiveDateEnd; // Optional
}