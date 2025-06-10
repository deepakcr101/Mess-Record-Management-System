package com.messmanagement.menu.dto;

import com.messmanagement.menu.entity.MealType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.util.Map;
import java.time.LocalDate;
import java.util.List; // For the inner map value type hint

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyMenuResponseDTO {
    // The structure will be Map<DayOfWeek, Map<MealType, List<MenuItemDTO>>>
    // For clarity in the DTO, we map MealType to a list of MenuItemDTOs
    private Map<DayOfWeek, Map<MealType, WeeklyMenuDayMealDTO>> dailyMenus;
    private LocalDate startDate; // The start date for which this weekly menu is relevant
    private LocalDate endDate;   // The end date for which this weekly menu is relevant
}