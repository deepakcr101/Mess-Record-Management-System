package com.messmanagement.mealentry.repository;

import com.messmanagement.mealentry.entity.MealEntry;
import com.messmanagement.menu.entity.MealType;
import com.messmanagement.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MealEntryRepository extends JpaRepository<MealEntry, Long>, JpaSpecificationExecutor<MealEntry> {

    /**
     * Finds all meal entries for a specific user, ordered by entry date and time descending.
     * Useful for the student's meal entry history.
     * Supports pagination.
     */
    Page<MealEntry> findByUserOrderByEntryDateDescEntryTimeDesc(User user, Pageable pageable);

    /**
     * Checks if a meal entry already exists for a given user, date, and meal type.
     * This is to prevent duplicate entries.
     */
    boolean existsByUserAndEntryDateAndMealType(User user, LocalDate entryDate, MealType mealType);

    // For admin viewing all meal entries, JpaRepository.findAll(Pageable pageable) and
    // JpaSpecificationExecutor<MealEntry> can be used for dynamic filtering by date, user, meal type, etc.
    // Example specific finders if not using specifications heavily for common cases:
    Page<MealEntry> findByEntryDate(LocalDate entryDate, Pageable pageable);
    Page<MealEntry> findByUser(User user, Pageable pageable); // Already covered by the one above if sorting is added
    Page<MealEntry> findByMealType(MealType mealType, Pageable pageable);
    Page<MealEntry> findByEntryDateAndMealType(LocalDate entryDate, MealType mealType, Pageable pageable);
    
    long countByEntryDate(LocalDate entryDate);
}