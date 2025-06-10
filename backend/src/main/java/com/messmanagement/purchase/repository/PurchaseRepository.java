package com.messmanagement.purchase.repository;

import com.messmanagement.purchase.entity.Purchase;
import com.messmanagement.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query; // <-- ADD THIS IMPORT
import org.springframework.data.repository.query.Param; // <-- ADD THIS IMPORT
import org.springframework.stereotype.Repository;

import java.math.BigDecimal; // <-- ADD THIS IMPORT
import java.time.LocalDateTime; // <-- ADD THIS IMPORT

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long>, JpaSpecificationExecutor<Purchase> {

    Page<Purchase> findByUserOrderByPurchaseDateDesc(User user, Pageable pageable);

    @Query("SELECT COALESCE(SUM(p.totalAmount), 0) FROM Purchase p WHERE p.purchaseDate >= :startDate AND p.purchaseDate <= :endDate")
    BigDecimal sumTotalAmountByPurchaseDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}