package com.messmanagement.menu.repository;

import com.messmanagement.menu.entity.MenuCategory;
import com.messmanagement.menu.entity.MenuItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // For complex dynamic queries
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long>, JpaSpecificationExecutor<MenuItem> {

    // Spring Data JPA will provide implementations for common CRUD operations.

    // Custom query methods can be added here as needed.
    // For example, to support filtering as mentioned in the plan:
    // "Student/Admin retrieves all menu items. Supports filtering (e.g., by category, availability)." [cite: 79]

    // Find by category
    Page<MenuItem> findByCategory(MenuCategory category, Pageable pageable);

    // Find by availability
    Page<MenuItem> findByIsAvailable(boolean isAvailable, Pageable pageable);

    // Find by category and availability
    Page<MenuItem> findByCategoryAndIsAvailable(MenuCategory category, boolean isAvailable, Pageable pageable);

    // We can also use JpaSpecificationExecutor for more complex, dynamic filtering if needed later.
}
