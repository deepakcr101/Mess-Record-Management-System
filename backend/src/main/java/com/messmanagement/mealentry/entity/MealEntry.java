package com.messmanagement.mealentry.entity;

import com.messmanagement.menu.entity.MealType; // We already have this enum
import com.messmanagement.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp; // For entry_time if we decide to use it like this

import java.time.LocalDate;
import java.time.LocalTime; // For entry_time
import java.time.LocalDateTime; // Alternative for a single timestamp

@Entity
@Table(name = "meal_entries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MealEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "entry_id")
    private Long entryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // The student who marked the entry

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false, length = 20)
    private MealType mealType;

    @Column(name = "entry_date", nullable = false) // Defaults to CURRENT_DATE in DB schema plan
    private LocalDate entryDate;

    @Column(name = "entry_time", nullable = false) // Defaults to CURRENT_TIME in DB schema plan
    private LocalTime entryTime;

    // Optional: If admin verification is part of the workflow, as per plan [cite: 56]
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by_admin_id", referencedColumnName = "user_id")
    private User verifiedByAdmin; // Admin who might verify the entry

    // Alternative to separate date and time, could be a single LocalDateTime
    // @CreationTimestamp
    // @Column(name = "entry_timestamp", nullable = false, updatable = false)
    // private LocalDateTime entryTimestamp;


    // PrePersist to set default date/time if not explicitly set
    @PrePersist
    protected void onCreate() {
        if (entryDate == null) {
            entryDate = LocalDate.now();
        }
        if (entryTime == null) {
            entryTime = LocalTime.now();
        }
    }
}