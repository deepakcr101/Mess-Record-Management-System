package com.messmanagement.menu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "menu_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;

    @Column(nullable = false)
    private String name;

    @Lob // For potentially longer text, though TEXT type in DB is fine
    private String description;

    @Column(nullable = false, precision = 10, scale = 2) // As per DECIMAL(10,2) [cite: 41]
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20) // Length matches VARCHAR(20) [cite: 42]
    private MenuCategory category;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_available", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean isAvailable = true; // Default to true [cite: 42]

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Relationships, e.g., with WeeklyMenuConfig, will be added later if needed directly on this entity.
    // The plan shows WeeklyMenuConfig referencing MenuItem via item_id.
}