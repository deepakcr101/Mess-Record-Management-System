package com.messmanagement.menu.controller;

import com.messmanagement.menu.dto.WeeklyMenuResponseDTO;
import com.messmanagement.menu.dto.WeeklyMenuSetupRequestDTO;
import com.messmanagement.menu.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.DayOfWeek; // Required for TemporalAdjusters

@RestController
@RequestMapping("/api/v1/weekly-menu")
@RequiredArgsConstructor
public class WeeklyMenuController {

    private final MenuService menuService;

    /**
     * Admin sets up or updates the weekly menu.
     * POST /api/v1/weekly-menu
     * Payload: WeeklyMenuSetupRequestDTO
     * Response: Success message or HTTP 200/201
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> setupWeeklyMenu(@Valid @RequestBody WeeklyMenuSetupRequestDTO setupRequest) {
        menuService.setupWeeklyMenu(setupRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body("Weekly menu setup processed successfully.");
    }

    /**
     * Student/Admin retrieves the weekly menu.
     * GET /api/v1/weekly-menu
     * Supports fetching for the week of a specific date, or a specific date range.
     * e.g., ?date=YYYY-MM-DD (gets the week containing this date)
     * e.g., ?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD
     * Response: WeeklyMenuResponseDTO
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()") // Accessible to any logged-in user
    public ResponseEntity<WeeklyMenuResponseDTO> getWeeklyMenu(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        WeeklyMenuResponseDTO weeklyMenu;

        if (startDate != null && endDate != null) {
            // If both startDate and endDate are provided, fetch for that specific range
            weeklyMenu = menuService.getMenuForDateRange(startDate, endDate);
        } else if (date != null) {
            // If only 'date' is provided, fetch for the week containing that date
            // (e.g., Monday to Sunday of that week)
            weeklyMenu = menuService.getWeeklyMenuForDate(date);
        } else {
            // Default behavior: fetch for the current week
            weeklyMenu = menuService.getWeeklyMenuForDate(LocalDate.now());
        }

        return ResponseEntity.ok(weeklyMenu);
    }
}