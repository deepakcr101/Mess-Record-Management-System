package com.messmanagement.user.service;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority; // Import @Lazy
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
import com.messmanagement.auth.entity.RevokedToken;
import com.messmanagement.auth.repository.RevokedTokenRepository;
import com.messmanagement.auth.util.JwtUtil;
import com.messmanagement.common.exception.ResourceNotFoundException;
import com.messmanagement.user.dto.AdminCreateUserRequestDTO;
import com.messmanagement.user.dto.AdminUpdateUserRequestDTO;
import com.messmanagement.user.dto.UserRegistrationRequestDTO;
import com.messmanagement.user.dto.UserResponseDTO;
import com.messmanagement.user.dto.UserUpdateRequestDTO;
import com.messmanagement.user.entity.Role;
import com.messmanagement.user.entity.User;
import com.messmanagement.user.repository.UserRepository;


@Service
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager; // Will be lazily injected
    private final JwtUtil jwtUtil;
private final RevokedTokenRepository revokedTokenRepository; // Inject

    // Update constructor
    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           @Lazy AuthenticationManager authenticationManager,
                           JwtUtil jwtUtil,
                           RevokedTokenRepository revokedTokenRepository) { // Add to constructor
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.revokedTokenRepository = revokedTokenRepository; // Assign
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
        String jti = jwtUtil.extractJti(refreshTokenValue);

        if (jti == null || revokedTokenRepository.existsByJti(jti)) { // Check if token JTI is in denylist
            throw new IllegalArgumentException("Refresh token is revoked or invalid.");
        }

        if (!jwtUtil.validateToken(refreshTokenValue)) {
            throw new IllegalArgumentException("Invalid or expired refresh token.");
        }
        // ... (rest of the method: extract userEmail, loadUserDetails, generate new tokens) ...
        String userEmail = jwtUtil.extractUsername(refreshTokenValue);
        UserDetails userDetails;
        try {
            userDetails = this.loadUserByUsername(userEmail);
        } catch (UsernameNotFoundException e) {
            throw new IllegalArgumentException("User for the refresh token not found.", e);
        }

        String newAccessToken = jwtUtil.generateAccessToken(userDetails);
        String newRefreshToken = jwtUtil.generateRefreshToken(userDetails); // Rotate refresh token

        // Important: Add the OLD refresh token's JTI to the denylist AFTER successful validation and new token generation
        // to prevent its reuse but allow the current operation to complete.
        // The expiry for the denylist record should be the expiry of the OLD refresh token.
        Date oldTokenExpiry = jwtUtil.extractExpiration(refreshTokenValue);
        revokedTokenRepository.save(new RevokedToken(jti, oldTokenExpiry.toInstant()));


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
    
    @Override
    @Transactional
    public void logoutUser(String refreshTokenValue) {
        if (refreshTokenValue == null || refreshTokenValue.isEmpty()) {
            return; // No token to invalidate
        }

        try {
            String jti = jwtUtil.extractJti(refreshTokenValue);
            Date expiryDate = jwtUtil.extractExpiration(refreshTokenValue); // Get expiry of the token being revoked

            if (jti != null && expiryDate != null && !revokedTokenRepository.existsByJti(jti)) {
                // Add to denylist only if not already there and valid structure
                // The expiryDate for the RevokedToken record is the original expiry of the refresh token itself.
                // This helps in cleaning up the RevokedToken table later.
                revokedTokenRepository.save(new RevokedToken(jti, expiryDate.toInstant()));
            }
        } catch (Exception e) {
            // Log error, but don't prevent logout flow if token is already malformed/expired
            // For example, using logger: logger.warn("Error processing refresh token during logout: " + e.getMessage());
        }
        // The client should also clear its access token and the refresh token cookie.
        // The backend's main job here is to blacklist the refresh token.
    }

    @Override
    @Transactional
    public UserResponseDTO updateUserProfile(String authenticatedUserEmail, UserUpdateRequestDTO updateRequest) {
        User user = userRepository.findByEmail(authenticatedUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + authenticatedUserEmail));

        // Update name if provided
        if (StringUtils.hasText(updateRequest.getName())) {
            user.setName(updateRequest.getName());
        }

        // Update mobile number if provided and different
        if (StringUtils.hasText(updateRequest.getMobileNo())) {
            if (!user.getMobileNo().equals(updateRequest.getMobileNo())) {
                // Check if the new mobile number is already taken by another user
                if (userRepository.existsByMobileNo(updateRequest.getMobileNo())) {
                    throw new IllegalArgumentException("Error: New mobile number is already in use by another account.");
                }
                user.setMobileNo(updateRequest.getMobileNo());
            }
        }

        // Update address if provided
        if (StringUtils.hasText(updateRequest.getAddress())) {
            user.setAddress(updateRequest.getAddress());
        }

        // Note: Email and password updates are not handled here.
        // MessProvidedUserId is also typically not updatable by the user themselves.

        User updatedUser = userRepository.save(user);
        return mapToUserResponseDTO(updatedUser);
    }
    
}