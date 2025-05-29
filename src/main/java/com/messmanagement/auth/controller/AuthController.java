package com.messmanagement.auth.controller;

import com.messmanagement.user.dto.UserRegistrationRequestDTO;
import com.messmanagement.user.dto.UserResponseDTO;
import com.messmanagement.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
// Import @Valid if spring-boot-starter-validation is added
// import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth") // Base path for all endpoints in this controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerStudent(
        /*@Valid*/ @RequestBody UserRegistrationRequestDTO registrationRequest) {
        // The @Valid annotation will trigger validation if spring-boot-starter-validation is on the classpath
        // and annotations are present in UserRegistrationRequestDTO.
        // We will add this dependency soon.

        UserResponseDTO registeredUser = userService.registerStudent(registrationRequest);
        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
    }

    // Login endpoint (/login) will be added here later
    // Password reset endpoints (/forgot-password, /reset-password) will also be added here
}
