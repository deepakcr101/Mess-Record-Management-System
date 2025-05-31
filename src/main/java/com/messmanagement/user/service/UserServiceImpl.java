package com.messmanagement.user.service;

import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder; // Import @Lazy
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.messmanagement.auth.dto.LoginRequestDTO;
import com.messmanagement.auth.dto.LoginResponseDTO;
import com.messmanagement.auth.util.JwtUtil;
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
}