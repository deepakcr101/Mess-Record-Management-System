package com.messmanagement.mealentry.service;

import com.messmanagement.common.exception.ResourceNotFoundException;
import com.messmanagement.mealentry.dto.MealEntryRequestDTO;
import com.messmanagement.mealentry.dto.MealEntryResponseDTO;
import com.messmanagement.mealentry.entity.MealEntry;
import com.messmanagement.mealentry.repository.MealEntryRepository;
import com.messmanagement.menu.entity.MealType;
import com.messmanagement.subscription.entity.Subscription;
import com.messmanagement.subscription.entity.SubscriptionStatus;
import com.messmanagement.subscription.repository.SubscriptionRepository;
import com.messmanagement.user.entity.User;
import com.messmanagement.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification; // For admin filtering
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.criteria.Predicate; // For Specification
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MealEntryServiceImpl implements MealEntryService {

    private final MealEntryRepository mealEntryRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;

    // Helper to map MealEntry entity to MealEntryResponseDTO
    private MealEntryResponseDTO mapToDTO(MealEntry mealEntry) {
        if (mealEntry == null) return null;
        return new MealEntryResponseDTO(
                mealEntry.getEntryId(),
                mealEntry.getUser().getUserId(),
                mealEntry.getUser().getEmail(),
                mealEntry.getUser().getName(),
                mealEntry.getMealType(),
                mealEntry.getEntryDate(),
                mealEntry.getEntryTime(),
                mealEntry.getVerifiedByAdmin() != null ? mealEntry.getVerifiedByAdmin().getUserId() : null,
                mealEntry.getVerifiedByAdmin() != null ? mealEntry.getVerifiedByAdmin().getName() : null
        );
    }

    @Override
    @Transactional
    public MealEntryResponseDTO markMealEntry(String authenticatedUserEmail, MealEntryRequestDTO requestDTO) {
        User user = userRepository.findByEmail(authenticatedUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + authenticatedUserEmail));

        // Validate mess_provided_user_id against the authenticated user's ID
        if (user.getMessProvidedUserId() == null || !user.getMessProvidedUserId().equals(requestDTO.getMessProvidedUserId())) {
            throw new IllegalArgumentException("Invalid Mess Provided User ID for the authenticated user.");
        }

        // Check for active subscription for the current date
        // The plan requires checking subscription: "if subscription expired should be shown about expired subscription and should be able to mark entry only after subscription"
        LocalDate currentDate = LocalDate.now();
        Optional<Subscription> activeSubscriptionOpt = subscriptionRepository
                .findByUserAndStatusIn(user, Arrays.asList(SubscriptionStatus.ACTIVE))
                .filter(sub -> !currentDate.isBefore(sub.getStartDate()) && !currentDate.isAfter(sub.getEndDate()));

        if (activeSubscriptionOpt.isEmpty()) {
            throw new IllegalStateException("No active subscription found for today. Please subscribe or renew.");
        }

        // Check if an entry for this mealType on this date already exists for this user
        if (mealEntryRepository.existsByUserAndEntryDateAndMealType(user, currentDate, requestDTO.getMealType())) {
            throw new IllegalStateException("Meal entry already marked for " + requestDTO.getMealType() + " on " + currentDate);
        }

        MealEntry mealEntry = new MealEntry();
        mealEntry.setUser(user);
        mealEntry.setMealType(requestDTO.getMealType());
        // entryDate and entryTime will be set by @PrePersist or can be set here explicitly
        mealEntry.setEntryDate(currentDate);
        mealEntry.setEntryTime(LocalTime.now());
        // verifiedByAdmin is initially null

        MealEntry savedEntry = mealEntryRepository.save(mealEntry);
        return mapToDTO(savedEntry);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MealEntryResponseDTO> getMyMealEntryHistory(String authenticatedUserEmail, Pageable pageable) {
        User user = userRepository.findByEmail(authenticatedUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + authenticatedUserEmail));
        Page<MealEntry> entries = mealEntryRepository.findByUserOrderByEntryDateDescEntryTimeDesc(user, pageable);
        return entries.map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MealEntryResponseDTO> getAllMealEntriesForAdmin(Pageable pageable, LocalDate filterDate, Long filterUserId, MealType filterMealType) {
        Specification<MealEntry> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filterDate != null) {
                predicates.add(criteriaBuilder.equal(root.get("entryDate"), filterDate));
            }
            if (filterUserId != null) {
                // Ensure User entity exists for this ID before creating predicate, or handle potential error
                User user = userRepository.findById(filterUserId).orElse(null);
                if (user != null) {
                     predicates.add(criteriaBuilder.equal(root.get("user"), user));
                } else {
                    // If filtering by a non-existent user ID, effectively return no results for this predicate.
                    // Or throw an exception if filtering by invalid ID is an error.
                    // For simplicity here, if user doesn't exist, this predicate might not be added or an empty result is implicitly handled.
                    // A robust way would be to check if user exists and if not, and filterUserId was specified, return empty page.
                }
            }
            if (filterMealType != null) {
                predicates.add(criteriaBuilder.equal(root.get("mealType"), filterMealType));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<MealEntry> entries = mealEntryRepository.findAll(spec, pageable);
        return entries.map(this::mapToDTO);
    }
}