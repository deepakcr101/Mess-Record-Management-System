package com.messmanagement.user.repository;

import java.util.Optional; // Import Role

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable; // Import Page
import org.springframework.data.jpa.repository.JpaRepository; // Import Pageable
import org.springframework.stereotype.Repository;

import com.messmanagement.user.entity.Role;
import com.messmanagement.user.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // ... existing methods ...
    Optional<User> findByEmail(String email);
    Optional<User> findByMobileNo(String mobileNo);
    Optional<User> findByMessProvidedUserId(String messProvidedUserId);
    boolean existsByEmail(String email);
    boolean existsByMobileNo(String mobileNo);
    boolean existsByMessProvidedUserId(String messProvidedUserId);

    // Method to find all users by a specific role with pagination
    Page<User> findByRole(Role role, Pageable pageable); // New method
}