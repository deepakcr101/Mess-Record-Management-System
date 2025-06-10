package com.messmanagement.report.service;

import com.messmanagement.purchase.repository.PurchaseRepository;
import com.messmanagement.subscription.entity.SubscriptionStatus;
import com.messmanagement.subscription.repository.SubscriptionRepository;
import com.messmanagement.user.entity.Role;
import com.messmanagement.user.repository.UserRepository;
import com.messmanagement.mealentry.repository.MealEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // Most report methods are read-only
public class ReportServiceImpl implements ReportService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final MealEntryRepository mealEntryRepository;
    private final PurchaseRepository purchaseRepository;

    @Override
    public long getTotalStudentCount() {
        return userRepository.countByRole(Role.STUDENT); // We'll need to add this method to UserRepository
    }

    @Override
    public long getActiveSubscriptionCount() {
        return subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE); // We'll need to add this
    }

    @Override
    public long getMealEntriesCountForDate(LocalDate date) {
        return mealEntryRepository.countByEntryDate(date); // We'll need to add this
    }

    @Override
    public BigDecimal getSalesSummary(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        BigDecimal subscriptionSales = subscriptionRepository.sumAmountPaidByStatusAndDateRange(
            SubscriptionStatus.ACTIVE, startDateTime, endDateTime);

        BigDecimal purchaseSales = purchaseRepository.sumTotalAmountByPurchaseDateBetween(
            startDateTime, endDateTime);

        // Handle nulls in case there are no sales of a certain type
        BigDecimal totalSales = BigDecimal.ZERO;
        if (subscriptionSales != null) {
            totalSales = totalSales.add(subscriptionSales);
        }
        if (purchaseSales != null) {
            totalSales = totalSales.add(purchaseSales);
        }
        return totalSales;
    }
}
