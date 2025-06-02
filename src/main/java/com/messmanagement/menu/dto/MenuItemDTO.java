package com.messmanagement.menu.dto;

import com.messmanagement.menu.entity.MenuCategory;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
// Import validation annotations when needed for request DTOs
// import jakarta.validation.constraints.NotBlank;
// import jakarta.validation.constraints.NotNull;
// import jakarta.validation.constraints.PositiveOrZero;
// import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemDTO {

    private Long itemId;

    // @NotBlank(message = "Menu item name cannot be blank")
    // @Size(max = 255, message = "Menu item name cannot exceed 255 characters")
    private String name;

    private String description;

    // @NotNull(message = "Price cannot be null")
    // @PositiveOrZero(message = "Price must be zero or positive")
    private BigDecimal price;

    // @NotNull(message = "Category cannot be null")
    private MenuCategory category;

    private String imageUrl;

    private boolean isAvailable = true; // Default to true

    private LocalDateTime createdAt; // For responses

    private LocalDateTime updatedAt; // For responses
}
