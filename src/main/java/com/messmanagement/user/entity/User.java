package com.messmanagement.user.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users", uniqueConstraints = { // [cite: 30]
    @UniqueConstraint(columnNames = "email"), // [cite: 31]
    @UniqueConstraint(columnNames = "mobile_no"), // [cite: 31]
    @UniqueConstraint(columnNames = "mess_provided_user_id") // [cite: 34]
})
@Data // Lombok: Generates getters, setters, toString, equals, hashCode
@NoArgsConstructor // Lombok: Generates no-args constructor
@AllArgsConstructor // Lombok: Generates all-args constructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Using IDENTITY for BIGSERIAL [cite: 30]
    @Column(name = "user_id")
    private Long userId; // [cite: 30]

    @Column(nullable = false)
    private String name; // [cite: 30]

    @Column(name = "mobile_no", nullable = false, unique = true)
    private String mobileNo; // [cite: 31]

    @Column(nullable = false, unique = true)
    private String email; // [cite: 31]

    @Column(nullable = false)
    private String address; // [cite: 32]

    @Column(name = "password_hash", nullable = false)
    private String passwordHash; // [cite: 32]

    @Enumerated(EnumType.STRING) // Store enum as String ("STUDENT", "ADMIN") [cite: 33]
    @Column(nullable = false, length = 10)
    private Role role; // [cite: 33]

    @Column(name = "mess_provided_user_id", unique = true)
    private String messProvidedUserId; // [cite: 34]

    @CreationTimestamp // Automatically set on creation
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // [cite: 35]

    @UpdateTimestamp // Automatically set on update
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt; // [cite: 36]

    // Relationships (e.g., with Subscription, Purchase, MealEntry) will be added later
    // when those entities are created.
}