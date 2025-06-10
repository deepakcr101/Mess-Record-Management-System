package com.messmanagement.menu.service;

import com.messmanagement.menu.dto.MenuItemDTO;
import com.messmanagement.menu.dto.WeeklyMenuResponseDTO; // Import
import com.messmanagement.menu.dto.WeeklyMenuSetupRequestDTO; // Import
import com.messmanagement.menu.entity.MenuCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate; // Import

public interface MenuService {

    // MenuItem CRUD
    MenuItemDTO addMenuItem(MenuItemDTO menuItemDTO);
    Page<MenuItemDTO> getAllMenuItems(Pageable pageable, MenuCategory category, Boolean isAvailable);
    MenuItemDTO getMenuItemById(Long itemId);
    MenuItemDTO updateMenuItem(Long itemId, MenuItemDTO menuItemDTO);
    void deleteMenuItem(Long itemId);

    // WeeklyMenu Management
    void setupWeeklyMenu(WeeklyMenuSetupRequestDTO setupRequest);
    WeeklyMenuResponseDTO getWeeklyMenuForDate(LocalDate date); // Get menu for the week containing this date
    WeeklyMenuResponseDTO getMenuForDateRange(LocalDate startDate, LocalDate endDate); // Get menu for a specific range
}