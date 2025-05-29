package com.messmanagement.user.service;

import com.messmanagement.user.dto.UserRegistrationRequestDTO;
import com.messmanagement.user.dto.UserResponseDTO;
import com.messmanagement.user.entity.Role;
import com.messmanagement.user.entity.User;
import com.messmanagement.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor // Lombok: Generates a constructor with required final fields
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // We'll configure this bean later in SecurityConfig

    @Override
    @Transactional // Good practice for operations that modify data
    public UserResponseDTO registerStudent(UserRegistrationRequestDTO registrationRequest) {
        // 1. Check if email already exists
        if (userRepository.existsByEmail(registrationRequest.getEmail())) {
            // We should define custom exceptions later as per the plan [cite: 291]
            throw new IllegalArgumentException("Error: Email is already in use!");
        }

        // 2. Check if mobile number already exists
        if (userRepository.existsByMobileNo(registrationRequest.getMobileNo())) {
            throw new IllegalArgumentException("Error: Mobile number is already in use!");
        }

        // 3. Check if messProvidedUserId already exists (if provided and needs to be unique)
        if (registrationRequest.getMessProvidedUserId() != null &&
            !registrationRequest.getMessProvidedUserId().isBlank() &&
            userRepository.existsByMessProvidedUserId(registrationRequest.getMessProvidedUserId())) {
            throw new IllegalArgumentException("Error: Mess Provided User ID is already in use!");
        }

        // 4. Create new user's account
        User user = new User();
        user.setName(registrationRequest.getName());
        user.setMobileNo(registrationRequest.getMobileNo());
        user.setEmail(registrationRequest.getEmail());
        user.setAddress(registrationRequest.getAddress());
        user.setPasswordHash(passwordEncoder.encode(registrationRequest.getPassword()));
        user.setRole(Role.STUDENT); // Default role for self-registration is STUDENT

        if (registrationRequest.getMessProvidedUserId() != null && !registrationRequest.getMessProvidedUserId().isBlank()) {
            user.setMessProvidedUserId(registrationRequest.getMessProvidedUserId());
        }
        // createdAt and updatedAt will be set automatically by @CreationTimestamp and @UpdateTimestamp

        // 5. Save user to the database
        User savedUser = userRepository.save(user);

        // 6. Map to UserResponseDTO
        return mapToUserResponseDTO(savedUser);
    }

    private UserResponseDTO mapToUserResponseDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setUserId(user.getUserId());
        dto.setName(user.getName());
        dto.setMobileNo(user.getMobileNo());
        dto.setEmail(user.getEmail());
        dto.setAddress(user.getAddress());
        dto.setRole(user.getRole());
        dto.setMessProvidedUserId(user.getMessProvidedUserId());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
}
