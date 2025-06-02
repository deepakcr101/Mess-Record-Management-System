package com.messmanagement.menu.service;

import com.messmanagement.common.exception.ResourceNotFoundException;
import com.messmanagement.menu.dto.*; // Import all DTOs from menu package
import com.messmanagement.menu.entity.MealType;
import com.messmanagement.menu.entity.MenuCategory;
import com.messmanagement.menu.entity.MenuItem;
import com.messmanagement.menu.entity.WeeklyMenuConfig; // Import
import com.messmanagement.menu.repository.MenuItemRepository;
import com.messmanagement.menu.repository.WeeklyMenuRepository; // Import
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuItemRepository menuItemRepository;
    private final WeeklyMenuRepository weeklyMenuRepository; // Inject WeeklyMenuRepository

    // ... (mapToDTO, mapToEntity for MenuItem, and MenuItem CRUD methods remain the same) ...
    // Helper method to map Entity to DTO
    private MenuItemDTO mapToDTO(MenuItem menuItem) {
        return new MenuItemDTO(
                menuItem.getItemId(),
                menuItem.getName(),
                menuItem.getDescription(),
                menuItem.getPrice(),
                menuItem.getCategory(),
                menuItem.getImageUrl(),
                menuItem.isAvailable(),
                menuItem.getCreatedAt(),
                menuItem.getUpdatedAt()
        );
    }

    // Helper method to map DTO to Entity (for create/update)
    private MenuItem mapToEntity(MenuItemDTO menuItemDTO) {
        MenuItem menuItem = new MenuItem();
        menuItem.setName(menuItemDTO.getName());
        menuItem.setDescription(menuItemDTO.getDescription());
        menuItem.setPrice(menuItemDTO.getPrice());
        menuItem.setCategory(menuItemDTO.getCategory());
        menuItem.setImageUrl(menuItemDTO.getImageUrl());
        menuItem.setAvailable(menuItemDTO.isAvailable());
        return menuItem;
    }

    @Override
    @Transactional
    public MenuItemDTO addMenuItem(MenuItemDTO menuItemDTO) {
        MenuItem menuItem = mapToEntity(menuItemDTO);
        MenuItem savedMenuItem = menuItemRepository.save(menuItem);
        return mapToDTO(savedMenuItem);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MenuItemDTO> getAllMenuItems(Pageable pageable, MenuCategory category, Boolean isAvailable) {
        Specification<MenuItem> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (category != null) {
                predicates.add(criteriaBuilder.equal(root.get("category"), category));
            }
            if (isAvailable != null) {
                predicates.add(criteriaBuilder.equal(root.get("isAvailable"), isAvailable));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        Page<MenuItem> menuItemsPage = menuItemRepository.findAll(spec, pageable);
        return menuItemsPage.map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public MenuItemDTO getMenuItemById(Long itemId) {
        MenuItem menuItem = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem not found with id: " + itemId));
        return mapToDTO(menuItem);
    }

    @Override
    @Transactional
    public MenuItemDTO updateMenuItem(Long itemId, MenuItemDTO menuItemDTO) {
        MenuItem existingMenuItem = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem not found with id: " + itemId + ". Cannot update."));
        if (StringUtils.hasText(menuItemDTO.getName())) existingMenuItem.setName(menuItemDTO.getName());
        if (StringUtils.hasText(menuItemDTO.getDescription())) existingMenuItem.setDescription(menuItemDTO.getDescription());
        if (menuItemDTO.getPrice() != null) existingMenuItem.setPrice(menuItemDTO.getPrice());
        if (menuItemDTO.getCategory() != null) existingMenuItem.setCategory(menuItemDTO.getCategory());
        if (menuItemDTO.getImageUrl() != null) existingMenuItem.setImageUrl(StringUtils.hasText(menuItemDTO.getImageUrl()) ? menuItemDTO.getImageUrl() : null);
        existingMenuItem.setAvailable(menuItemDTO.isAvailable());
        MenuItem updatedMenuItem = menuItemRepository.save(existingMenuItem);
        return mapToDTO(updatedMenuItem);
    }

    @Override
    @Transactional
    public void deleteMenuItem(Long itemId) {
        if (!menuItemRepository.existsById(itemId)) {
            throw new ResourceNotFoundException("MenuItem not found with id: " + itemId + ". Cannot delete.");
        }
        // TODO: Consider implications on WeeklyMenuConfig that use this item.
        // Delete related WeeklyMenuConfig entries or set their item_id to null / handle error?
        // For now, potential for DataIntegrityViolationException if item is in use.
        weeklyMenuRepository.deleteAll(weeklyMenuRepository.findAll().stream().filter(wmc -> wmc.getMenuItem().getItemId().equals(itemId)).collect(Collectors.toList())); // Example cleanup
        menuItemRepository.deleteById(itemId);
    }


    // --- WeeklyMenu Management Implementation ---

    @Override
    @Transactional
    public void setupWeeklyMenu(WeeklyMenuSetupRequestDTO setupRequest) {
        List<WeeklyMenuConfig> configsToSave = new ArrayList<>();

        for (WeeklyMenuItemEntryDTO entry : setupRequest.getMenuEntries()) {
            MenuItem menuItem = menuItemRepository.findById(entry.getItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("MenuItem not found with id: " + entry.getItemId() + " for weekly setup."));

            // Optional: Delete existing configurations for this specific slot and effectiveDateStart before adding new one
            // to prevent issues with the composite unique key or to ensure a clean "update".
            // This depends on how "update" is defined. If it's a full replacement for a slot/date:
            List<WeeklyMenuConfig> existing = weeklyMenuRepository.findByDayOfWeekAndMealTypeAndMenuItem_ItemIdAndEffectiveDateStart(
                entry.getDayOfWeek(), entry.getMealType(), entry.getItemId(), entry.getEffectiveDateStart()
            );
            // A simpler approach for "update" might be to delete all entries for a given day/meal/effectiveDateStart and then add new ones.
            // For now, we assume new entries, and the unique constraint will prevent exact duplicates.
            // For a robust update, you'd likely clear existing entries for a DayOfWeek, MealType, and effectiveDateStart range.

            WeeklyMenuConfig config = new WeeklyMenuConfig();
            config.setMenuItem(menuItem);
            config.setDayOfWeek(entry.getDayOfWeek());
            config.setMealType(entry.getMealType());
            config.setEffectiveDateStart(entry.getEffectiveDateStart());
            config.setEffectiveDateEnd(entry.getEffectiveDateEnd()); // Can be null

            configsToSave.add(config);
        }
        weeklyMenuRepository.saveAll(configsToSave);
    }

    @Override
    @Transactional(readOnly = true)
    public WeeklyMenuResponseDTO getWeeklyMenuForDate(LocalDate date) {
        // Determine the start (e.g., Monday) and end (e.g., Sunday) of the week containing the given date.
        LocalDate weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        return getMenuForDateRange(weekStart, weekEnd);
    }

    @Override
    @Transactional(readOnly = true)
    public WeeklyMenuResponseDTO getMenuForDateRange(LocalDate startDate, LocalDate endDate) {
        List<WeeklyMenuConfig> activeConfigs = weeklyMenuRepository.findActiveConfigsForDateRange(startDate, endDate);

        // Group configurations by DayOfWeek -> MealType -> List of MenuItems
        Map<DayOfWeek, Map<MealType, List<MenuItemDTO>>> dailyMenusMap = new EnumMap<>(DayOfWeek.class);

        for (DayOfWeek day : DayOfWeek.values()) { // Iterate through all days to ensure all days are present in the map
            Map<MealType, List<MenuItemDTO>> mealsForDay = new EnumMap<>(MealType.class);
            for (MealType meal : MealType.values()) { // Ensure all meal types are present
                mealsForDay.put(meal, new ArrayList<>());
            }
            dailyMenusMap.put(day, mealsForDay);
        }
        
        for (WeeklyMenuConfig config : activeConfigs) {
            // Filter for items that are within the specific requested range [startDate, endDate]
            // and specifically for the days of that week that fall within the range.
            // This loop iterates through each day from startDate to endDate.
            for (LocalDate currentDate = startDate; !currentDate.isAfter(endDate); currentDate = currentDate.plusDays(1)) {
                if (config.getDayOfWeek() == currentDate.getDayOfWeek() &&
                    config.getEffectiveDateStart().compareTo(currentDate) <= 0 &&
                    (config.getEffectiveDateEnd() == null || config.getEffectiveDateEnd().compareTo(currentDate) >= 0)) {
                    
                    Map<MealType, List<MenuItemDTO>> meals = dailyMenusMap
                        .computeIfAbsent(config.getDayOfWeek(), k -> new EnumMap<>(MealType.class));
                    
                    List<MenuItemDTO> items = meals
                        .computeIfAbsent(config.getMealType(), k -> new ArrayList<>());
                    
                    items.add(mapToDTO(config.getMenuItem())); // Add the DTO
                }
            }
        }
        
        // Post-process to ensure the inner DTO WeeklyMenuDayMealDTO is used
         Map<DayOfWeek, Map<MealType, WeeklyMenuDayMealDTO>> finalDailyMenusMap = new EnumMap<>(DayOfWeek.class);
         dailyMenusMap.forEach((day, meals) -> {
             Map<MealType, WeeklyMenuDayMealDTO> finalMeals = new EnumMap<>(MealType.class);
             meals.forEach((mealType, items) -> finalMeals.put(mealType, new WeeklyMenuDayMealDTO(items)));
             finalDailyMenusMap.put(day, finalMeals);
         });

        return new WeeklyMenuResponseDTO(finalDailyMenusMap, startDate, endDate);
    }
}
