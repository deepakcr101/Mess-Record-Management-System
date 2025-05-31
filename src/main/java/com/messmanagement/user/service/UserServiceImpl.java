package com.messmanagement.user.service;

import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority; // Import @Lazy
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.messmanagement.auth.dto.LoginRequestDTO;
import com.messmanagement.auth.dto.LoginResponseDTO;
import com.messmanagement.auth.dto.TokenRefreshResponseDTO;
import com.messmanagement.auth.util.JwtUtil;
import com.messmanagement.common.exception.ResourceNotFoundException;
import com.messmanagement.user.dto.AdminCreateUserRequestDTO;
import com.messmanagement.user.dto.AdminUpdateUserRequestDTO;
import com.messmanagement.user.dto.UserRegistrationRequestDTO;
import com.messmanagement.user.dto.UserResponseDTO;
import com.messmanagement.user.entity.Role;
import com.messmanagement.user.entity.User;
import com.messmanagement.user.repository.UserRepository;


@Service
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager; // Will be lazily injected
    private final JwtUtil jwtUtil;

    // Modify constructor to use @Lazy for AuthenticationManager
    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           @Lazy AuthenticationManager authenticationManager, // Add @Lazy here
                           JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @Override
    @Transactional
    public UserResponseDTO registerStudent(UserRegistrationRequestDTO registrationRequest) {
        // ... (registration logic remains the same)
        if (userRepository.existsByEmail(registrationRequest.getEmail())) {
            throw new IllegalArgumentException("Error: Email is already in use!");
        }
        if (userRepository.existsByMobileNo(registrationRequest.getMobileNo())) {
            throw new IllegalArgumentException("Error: Mobile number is already in use!");
        }
        if (registrationRequest.getMessProvidedUserId() != null &&
            !registrationRequest.getMessProvidedUserId().isBlank() &&
            userRepository.existsByMessProvidedUserId(registrationRequest.getMessProvidedUserId())) {
            throw new IllegalArgumentException("Error: Mess Provided User ID is already in use!");
        }
        User user = new User();
        user.setName(registrationRequest.getName());
        user.setMobileNo(registrationRequest.getMobileNo());
        user.setEmail(registrationRequest.getEmail());
        user.setAddress(registrationRequest.getAddress());
        user.setPasswordHash(passwordEncoder.encode(registrationRequest.getPassword()));
        user.setRole(Role.STUDENT);
        if (registrationRequest.getMessProvidedUserId() != null && !registrationRequest.getMessProvidedUserId().isBlank()) {
            user.setMessProvidedUserId(registrationRequest.getMessProvidedUserId());
        }
        User savedUser = userRepository.save(user);
        return mapToUserResponseDTO(savedUser);
    }

    @Override
    @Transactional
    public LoginResponseDTO loginUser(LoginRequestDTO loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found after authentication: " + username));
        String accessToken = jwtUtil.generateAccessToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);
        return new LoginResponseDTO(
                accessToken,
                refreshToken,
                "Bearer",
                user.getUserId(),
                user.getEmail(),
                user.getRole().name()
        );
    }

    private UserResponseDTO mapToUserResponseDTO(User user) {
        // ... (mapping logic remains the same)
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

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // ... (loadUserByUsername logic remains the same)
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with email: " + email));
        List<GrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                authorities
        );
    }

    @Override
    public TokenRefreshResponseDTO refreshToken(String refreshTokenValue) {
        // 1. Validate the incoming refresh token (integrity, expiry)
        if (!jwtUtil.validateToken(refreshTokenValue)) {
            // This simple validation only checks expiry and signature based on current JwtUtil.
            // For a robust rotation, if this token was already used (and we were tracking it),
            // we might invalidate the entire session (all refresh tokens for the user).
            throw new IllegalArgumentException("Invalid or expired refresh token.");
        }

        String userEmail = jwtUtil.extractUsername(refreshTokenValue);
        UserDetails userDetails;
        try {
            userDetails = this.loadUserByUsername(userEmail); // Verifies user still exists and is valid
        } catch (UsernameNotFoundException e) {
            throw new IllegalArgumentException("User for the refresh token not found.", e);
        }

        // Optional: Further checks if the refresh token is blacklisted/revoked.
        // For now, we assume if it passes signature/expiry and user exists, it's good for one-time use in rotation.

        // 2. Generate a NEW Access Token
        String newAccessToken = jwtUtil.generateAccessToken(userDetails);

        // 3. Generate a NEW Refresh Token (Rotation)
        String newRefreshToken = jwtUtil.generateRefreshToken(userDetails);

        return new TokenRefreshResponseDTO(newAccessToken, newRefreshToken, "Bearer");
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserProfileByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return mapToUserResponseDTO(user); // We already have a mapper for this
    }
    
     @Override
    @Transactional
    // Method security can also be applied at the service layer
    // @PreAuthorize("hasRole('ADMIN')") // Alternative to controller-level security
    public UserResponseDTO adminCreateUser(AdminCreateUserRequestDTO createRequest) {
        if (userRepository.existsByEmail(createRequest.getEmail())) {
            throw new IllegalArgumentException("Error: Email is already in use!");
        }
        if (userRepository.existsByMobileNo(createRequest.getMobileNo())) {
            throw new IllegalArgumentException("Error: Mobile number is already in use!");
        }
        if (createRequest.getMessProvidedUserId() != null &&
            !createRequest.getMessProvidedUserId().isBlank() &&
            userRepository.existsByMessProvidedUserId(createRequest.getMessProvidedUserId())) {
            throw new IllegalArgumentException("Error: Mess Provided User ID is already in use!");
        }

        User user = new User();
        user.setName(createRequest.getName());
        user.setMobileNo(createRequest.getMobileNo());
        user.setEmail(createRequest.getEmail());
        user.setAddress(createRequest.getAddress());
        user.setPasswordHash(passwordEncoder.encode(createRequest.getPassword()));
        user.setRole(createRequest.getRole() != null ? createRequest.getRole() : Role.STUDENT); // Use provided role or default

        if (createRequest.getMessProvidedUserId() != null && !createRequest.getMessProvidedUserId().isBlank()) {
            user.setMessProvidedUserId(createRequest.getMessProvidedUserId());
        }

        User savedUser = userRepository.save(user);
        return mapToUserResponseDTO(savedUser);
    }
    
    @Override
    @Transactional(readOnly = true)
    // @PreAuthorize("hasRole('ADMIN')") // Also an option for service-level security
    public Page<UserResponseDTO> getAllStudents(Pageable pageable) {
        Page<User> studentPage = userRepository.findByRole(Role.STUDENT, pageable);
        return studentPage.map(this::mapToUserResponseDTO); // Convert Page<User> to Page<UserResponseDTO>
    }
    
    @Override
    @Transactional(readOnly = true)
    // @PreAuthorize("hasRole('ADMIN')") // Can be here or on controller
    public UserResponseDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        // We might want to restrict this to only fetch students if that's the intent,
        // or allow admins to fetch any user. For now, it fetches any user by ID.
        // If only students:
        // if (!user.getRole().equals(Role.STUDENT)) {
        //     throw new ResourceNotFoundException("Student not found with id: " + userId + " (user is not a student)");
        // }
        return mapToUserResponseDTO(user);
    }
    
    @Override
    @Transactional
    // @PreAuthorize("hasRole('ADMIN')")
    public UserResponseDTO adminUpdateUser(Long userId, AdminUpdateUserRequestDTO updateRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Update fields if they are provided in the request
        if (StringUtils.hasText(updateRequest.getName())) {
            user.setName(updateRequest.getName());
        }
        if (StringUtils.hasText(updateRequest.getMobileNo())) {
            // Add validation for uniqueness if mobileNo is changed and different from current
            if (!user.getMobileNo().equals(updateRequest.getMobileNo()) && userRepository.existsByMobileNo(updateRequest.getMobileNo())) {
                throw new IllegalArgumentException("Error: New mobile number is already in use!");
            }
            user.setMobileNo(updateRequest.getMobileNo());
        }
        if (StringUtils.hasText(updateRequest.getEmail())) {
            // Add validation for uniqueness if email is changed and different from current
            if (!user.getEmail().equals(updateRequest.getEmail()) && userRepository.existsByEmail(updateRequest.getEmail())) {
                throw new IllegalArgumentException("Error: New email is already in use!");
            }
            user.setEmail(updateRequest.getEmail());
        }
        if (StringUtils.hasText(updateRequest.getAddress())) {
            user.setAddress(updateRequest.getAddress());
        }
        if (updateRequest.getMessProvidedUserId() != null) { // Allow setting to blank/null if intended
             // Add validation for uniqueness if messProvidedUserId is changed, not blank, and different from current
            if (StringUtils.hasText(updateRequest.getMessProvidedUserId()) &&
                (user.getMessProvidedUserId() == null || !user.getMessProvidedUserId().equals(updateRequest.getMessProvidedUserId())) &&
                userRepository.existsByMessProvidedUserId(updateRequest.getMessProvidedUserId())) {
                throw new IllegalArgumentException("Error: New Mess Provided User ID is already in use!");
            }
            user.setMessProvidedUserId(StringUtils.hasText(updateRequest.getMessProvidedUserId()) ? updateRequest.getMessProvidedUserId() : null);
        }
        if (updateRequest.getRole() != null) {
            user.setRole(updateRequest.getRole());
        }
        // If password update was included in DTO:
        // if (StringUtils.hasText(updateRequest.getPassword())) {
        //     user.setPasswordHash(passwordEncoder.encode(updateRequest.getPassword()));
        // }

        // updatedAt will be handled by @UpdateTimestamp
        User updatedUser = userRepository.save(user);
        return mapToUserResponseDTO(updatedUser);
    }
    
    @Override
    @Transactional
    // @PreAuthorize("hasRole('ADMIN')")
    public void deleteUserById(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId + ". Cannot delete.");
        }
        // Considerations: What happens to related entities?
        // e.g., Subscriptions, Purchases, MealEntries.
        // Depending on cascade rules or business logic, you might need to handle these.
        // For now, a direct delete. If foreign key constraints prevent deletion due to related data,
        // this will fail at the DB level.
        userRepository.deleteById(userId);
    }
    
}