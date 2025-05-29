package com.messmanagement.user.repository;

import com.messmanagement.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Data JPA will automatically implement a method to find a user by email
    Optional<User> findByEmail(String email);

    // Spring Data JPA will automatically implement a method to find a user by mobile number
    Optional<User> findByMobileNo(String mobileNo);

    // Spring Data JPA will automatically implement a method to find a user by mess_provided_user_id
    Optional<User> findByMessProvidedUserId(String messProvidedUserId);

    // You can add more custom query methods here if needed
    // For example, checking if an email or mess_provided_user_id already exists:
    boolean existsByEmail(String email);
    boolean existsByMobileNo(String mobileNo);
    boolean existsByMessProvidedUserId(String messProvidedUserId);

}
