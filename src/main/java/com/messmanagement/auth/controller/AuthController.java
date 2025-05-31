package com.messmanagement.auth.controller;

import com.messmanagement.auth.dto.LoginRequestDTO; 
import com.messmanagement.auth.dto.LoginResponseDTO;
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

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> loginUser(
        /*@Valid*/ @RequestBody LoginRequestDTO loginRequest) {
        // The @Valid annotation will trigger validation for LoginRequestDTO if configured

        LoginResponseDTO loginResponse = userService.loginUser(loginRequest);
        // Note on refresh token:
        // The project plan (source 38, 119) suggests HttpOnly cookies for refresh tokens.
        // We currently return it in the response body.
        // We can refine this later to set it as an HttpOnly cookie from the backend.
        // For example, using HttpServletResponse.addCookie(...)
        return ResponseEntity.ok(loginResponse);
    }
    
    // Password reset endpoints (/forgot-password, /reset-password) will also be added here
}
