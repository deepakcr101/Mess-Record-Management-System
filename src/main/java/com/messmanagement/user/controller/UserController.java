package com.messmanagement.user.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.messmanagement.user.dto.AdminCreateUserRequestDTO;
import com.messmanagement.user.dto.AdminUpdateUserRequestDTO;
import com.messmanagement.user.dto.UserResponseDTO;
import com.messmanagement.user.dto.UserUpdateRequestDTO;
import com.messmanagement.user.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

     private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(Authentication authentication) {
        // ... (existing /me endpoint code) ...
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();
        UserResponseDTO userProfile = userService.getUserProfileByEmail(email);
        return ResponseEntity.ok(userProfile);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> adminCreateUser(
            @RequestBody AdminCreateUserRequestDTO createRequest) {
        UserResponseDTO createdUser = userService.adminCreateUser(createRequest);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponseDTO>> getAllStudents(
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        // Spring automatically resolves Pageable from request parameters like ?page=0&size=10&sort=name,asc
        // @PageableDefault provides default values if not specified in the request.
        Page<UserResponseDTO> students = userService.getAllStudents(pageable);
        return ResponseEntity.ok(students);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long userId) {
        UserResponseDTO user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }
    
    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> adminUpdateUser(
            @PathVariable Long userId,
            /* @Valid */ @RequestBody AdminUpdateUserRequestDTO updateRequest) {
        UserResponseDTO updatedUser = userService.adminUpdateUser(userId, updateRequest);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUserById(@PathVariable Long userId) {
        userService.deleteUserById(userId);
        return ResponseEntity.noContent().build(); // HTTP 204 No Content on successful deletion
    }
    
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()") // Ensures the user is authenticated
    public ResponseEntity<UserResponseDTO> updateCurrentUserProfile(
            Authentication authentication,
            /* @Valid */ @RequestBody UserUpdateRequestDTO updateRequest) {

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername(); // Get email of the authenticated user

        UserResponseDTO updatedUserProfile = userService.updateUserProfile(email, updateRequest);
        return ResponseEntity.ok(updatedUserProfile);
    }

}