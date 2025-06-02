package com.messmanagement.menu.repository;

import com.messmanagement.menu.entity.MealType;
import com.messmanagement.menu.entity.WeeklyMenuConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface WeeklyMenuRepository extends JpaRepository<WeeklyMenuConfig, Long>, JpaSpecificationExecutor<WeeklyMenuConfig> {

    // Standard CRUD operations are inherited from JpaRepository.

    // Custom query methods for fetching weekly menu configurations:

    /**
     * Finds menu configurations for a specific date, day of the week, and meal type.
     * This query considers the effective start and end dates of the menu configuration.
     */
    @Query("SELECT wmc FROM WeeklyMenuConfig wmc " +
           "WHERE wmc.dayOfWeek = :dayOfWeek " +
           "AND wmc.mealType = :mealType " +
           "AND wmc.effectiveDateStart <= :date " +
           "AND (wmc.effectiveDateEnd IS NULL OR wmc.effectiveDateEnd >= :date)")
    List<WeeklyMenuConfig> findConfigForDateAndMealType(
            @Param("date") LocalDate date,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("mealType") MealType mealType
    );

    /**
     * Finds all menu configurations active within a given date range.
     * Useful for fetching a menu for a whole week or a specific period.
     * A configuration is considered active if its period overlaps with the given [startDate, endDate] range.
     * Overlap condition: (wmc.effectiveDateStart <= endDate) AND (wmc.effectiveDateEnd IS NULL OR wmc.effectiveDateEnd >= startDate)
     */
    @Query("SELECT wmc FROM WeeklyMenuConfig wmc " +
           "WHERE wmc.effectiveDateStart <= :endDate " +
           "AND (wmc.effectiveDateEnd IS NULL OR wmc.effectiveDateEnd >= :startDate)")
    List<WeeklyMenuConfig> findActiveConfigsForDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // You might also want methods to find configurations by specific item,
    // or to delete configurations for a certain day/meal/item before updating.
    // Example: Find by day, meal type, and item to check for existing conflicting configurations before adding a new one.
    List<WeeklyMenuConfig> findByDayOfWeekAndMealTypeAndMenuItem_ItemIdAndEffectiveDateStart(
            DayOfWeek dayOfWeek, MealType mealType, Long itemId, LocalDate effectiveDateStart
    );
    
    // Find by day, meal type, and effective start date (to help with replacing a whole meal's items)
    List<WeeklyMenuConfig> findByDayOfWeekAndMealTypeAndEffectiveDateStart(
            DayOfWeek dayOfWeek, MealType mealType, LocalDate effectiveDateStart
    );

}