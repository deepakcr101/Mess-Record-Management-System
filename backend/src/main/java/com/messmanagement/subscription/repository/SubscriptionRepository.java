package com.messmanagement.subscription.repository;

import com.messmanagement.subscription.entity.Subscription;
import com.messmanagement.subscription.entity.SubscriptionStatus;
import com.messmanagement.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query; // <-- ADD THIS IMPORT
import org.springframework.data.repository.query.Param; // <-- ADD THIS IMPORT
import org.springframework.stereotype.Repository;

import java.math.BigDecimal; // <-- ADD THIS IMPORT
import java.time.LocalDate;
import java.time.LocalDateTime; // <-- ADD THIS IMPORT
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long>, JpaSpecificationExecutor<Subscription> {

    // ... (existing methods like findByUserAndStatusIn, findFirstByUserOrderByEndDateDesc, etc.) ...
    
    Optional<Subscription> findByUserAndStatusIn(User user, List<SubscriptionStatus> statuses);
    Optional<Subscription> findFirstByUserOrderByEndDateDesc(User user);
    List<Subscription> findByUser(User user);
    Page<Subscription> findByStatus(SubscriptionStatus status, Pageable pageable);
    List<Subscription> findByEndDateBeforeAndStatus(LocalDate date, SubscriptionStatus status);
    Optional<Subscription> findByStripeSubscriptionId(String stripeSubscriptionId);
    long countByStatus(SubscriptionStatus status);

    @Query("SELECT COALESCE(SUM(s.amountPaid), 0) FROM Subscription s WHERE s.status = :status AND s.createdAt >= :startDate AND s.createdAt <= :endDate")
    BigDecimal sumAmountPaidByStatusAndDateRange(@Param("status") SubscriptionStatus status, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}