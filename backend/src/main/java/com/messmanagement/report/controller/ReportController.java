package com.messmanagement.report.controller;

import com.messmanagement.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/students/count")
    public ResponseEntity<Map<String, Long>> getTotalStudentCount() {
        long count = reportService.getTotalStudentCount();
        return ResponseEntity.ok(Map.of("totalStudents", count));
    }

    @GetMapping("/subscriptions/active-count")
    public ResponseEntity<Map<String, Long>> getActiveSubscriptionCount() {
        long count = reportService.getActiveSubscriptionCount();
        return ResponseEntity.ok(Map.of("activeSubscriptions", count));
    }

    // --- THIS IS THE CORRECTED METHOD ---
    @GetMapping("/meal-entries/daily-count")
    public ResponseEntity<Map<String, Object>> getMealEntriesCountForDate( // Changed Long to Object
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        long count = reportService.getMealEntriesCountForDate(date);
        return ResponseEntity.ok(Map.of(
                "date", date.toString(), // This is a String
                "mealEntriesCount", count      // This is a Long
        ));
    }

    @GetMapping("/sales/summary")
    public ResponseEntity<Map<String, Object>> getSalesSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        BigDecimal totalSales = reportService.getSalesSummary(startDate, endDate);
        return ResponseEntity.ok(Map.of(
            "startDate", startDate.toString(),
            "endDate", endDate.toString(),
            "totalSales", totalSales
        ));
    }
}