package com.messmanagement.menu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyMenuDayMealDTO {
    private List<MenuItemDTO> items;
}