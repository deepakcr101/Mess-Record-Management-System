package com.messmanagement.report.service;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface ReportService {
    long getTotalStudentCount();
    long getActiveSubscriptionCount();
    long getMealEntriesCountForDate(LocalDate date);
    // We can create a DTO for sales summary if it gets complex
    BigDecimal getSalesSummary(LocalDate startDate, LocalDate endDate);
}