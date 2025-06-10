package com.messmanagement.menu.controller;

import com.messmanagement.menu.dto.MenuItemDTO;
import com.messmanagement.menu.entity.MenuCategory;
import com.messmanagement.menu.service.MenuService;
import jakarta.validation.Valid; // For request body validation
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/menu-items")
@RequiredArgsConstructor
public class MenuItemsController {

    private final MenuService menuService;

    /**
     * Admin adds a new menu item.
     * POST /api/v1/menu-items
     * Payload: MenuItemDTO, Response: MenuItemDTO
     * Secured for ADMIN.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuItemDTO> addMenuItem(@Valid @RequestBody MenuItemDTO menuItemDTO) {
        MenuItemDTO createdMenuItem = menuService.addMenuItem(menuItemDTO);
        return new ResponseEntity<>(createdMenuItem, HttpStatus.CREATED);
    }

    /**
     * Student/Admin retrieves all menu items.
     * GET /api/v1/menu-items
     * Supports filtering by category and availability. Response: Page<MenuItemDTO>
     * Secured for any authenticated user.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()") // Accessible to any logged-in user (Student or Admin)
    public ResponseEntity<Page<MenuItemDTO>> getAllMenuItems(
            @PageableDefault(size = 10, sort = "name") Pageable pageable,
            @RequestParam(required = false) MenuCategory category,
            @RequestParam(required = false) Boolean isAvailable) {
        Page<MenuItemDTO> menuItems = menuService.getAllMenuItems(pageable, category, isAvailable);
        return ResponseEntity.ok(menuItems);
    }

    /**
     * Student/Admin retrieves a specific menu item.
     * GET /api/v1/menu-items/{itemId}
     * Response: MenuItemDTO
     * Secured for any authenticated user.
     */
    @GetMapping("/{itemId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MenuItemDTO> getMenuItemById(@PathVariable Long itemId) {
        MenuItemDTO menuItem = menuService.getMenuItemById(itemId);
        return ResponseEntity.ok(menuItem);
    }

    /**
     * Admin updates a menu item.
     * PUT /api/v1/menu-items/{itemId}
     * Payload: MenuItemDTO, Response: MenuItemDTO
     * Secured for ADMIN.
     */
    @PutMapping("/{itemId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuItemDTO> updateMenuItem(
            @PathVariable Long itemId,
            @Valid @RequestBody MenuItemDTO menuItemDTO) {
        MenuItemDTO updatedMenuItem = menuService.updateMenuItem(itemId, menuItemDTO);
        return ResponseEntity.ok(updatedMenuItem);
    }

    /**
     * Admin removes a menu item.
     * DELETE /api/v1/menu-items/{itemId}
     * Response: HTTP 204 No Content
     * Secured for ADMIN.
     */
    @DeleteMapping("/{itemId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Long itemId) {
        menuService.deleteMenuItem(itemId);
        return ResponseEntity.noContent().build();
    }
}
