package com.messmanagement.auth.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping; // Import Cookie
import org.springframework.web.bind.annotation.RequestBody; // Import HttpServletResponse
import org.springframework.web.bind.annotation.RequestMapping; // Import
import org.springframework.web.bind.annotation.RestController; // To read cookies

import com.messmanagement.auth.dto.AccessTokenResponseDTO; // To read specific cookie
import com.messmanagement.auth.dto.LoginRequestDTO;
import com.messmanagement.auth.dto.LoginResponseDTO;
import com.messmanagement.auth.dto.LoginSuccessBodyResponseDTO;
import com.messmanagement.auth.dto.TokenRefreshResponseDTO;
import com.messmanagement.user.dto.UserRegistrationRequestDTO;
import com.messmanagement.user.dto.UserResponseDTO; // For building SameSite cookies
import com.messmanagement.user.service.UserService; // For setting cookie header

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @Value("${jwt.refresh.token.expiration.ms}")
    private long refreshTokenExpirationMs;
    
    // TODO: Define a dedicated property for refresh cookie secure flag, e.g., in application.properties
    @Value("${app.cookie.secure:false}") // Default to false for dev, set to true in prod profile
    private boolean refreshTokenCookieSecure;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerStudent(
        @RequestBody UserRegistrationRequestDTO registrationRequest) {
        UserResponseDTO registeredUser = userService.registerStudent(registrationRequest);
        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
    }


    @PostMapping("/login")
    public ResponseEntity<?> loginUser(
        @RequestBody LoginRequestDTO loginRequest,
        HttpServletResponse httpServletResponse
    ) {
        LoginResponseDTO loginResponsePayload = userService.loginUser(loginRequest);

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", loginResponsePayload.getRefreshToken())
                .httpOnly(true)
                .secure(refreshTokenCookieSecure) // Use configured value
                .path("/api/v1/auth") // Scope cookie to auth paths
                .maxAge(refreshTokenExpirationMs / 1000)
                .sameSite("Strict")
                .build();
        httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        LoginSuccessBodyResponseDTO responseBody = new LoginSuccessBodyResponseDTO(
            loginResponsePayload.getAccessToken(),
            loginResponsePayload.getTokenType(),
            loginResponsePayload.getUserId(),
            loginResponsePayload.getEmail(),
            loginResponsePayload.getRole()
        );
        return ResponseEntity.ok(responseBody);
    }


    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(
            @CookieValue(name = "refreshToken", required = false) String oldRefreshTokenFromCookie,
            HttpServletResponse httpServletResponse // Inject to set the new cookie
           ) {

        if (oldRefreshTokenFromCookie == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token cookie is missing.");
        }
        
        try {
            TokenRefreshResponseDTO refreshResponse = userService.refreshToken(oldRefreshTokenFromCookie);

            // Set the NEW refresh token as an HttpOnly cookie
            ResponseCookie newRefreshTokenCookie = ResponseCookie.from("refreshToken", refreshResponse.getNewRefreshToken())
                    .httpOnly(true)
                    .secure(refreshTokenCookieSecure) // Use configured value
                    .path("/api/v1/auth") // Consistent path
                    .maxAge(refreshTokenExpirationMs / 1000)
                    .sameSite("Strict")
                    .build();
            httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, newRefreshTokenCookie.toString());

            // The response body will only contain the new access token and its type.
            // Create a DTO specifically for the body response of refresh token if needed,
            // or adjust TokenRefreshResponseDTO to not expose the new refresh token if it's only via cookie.
            // For now, let's create a simpler response for the body.
            AccessTokenResponseDTO accessTokenResponse = new AccessTokenResponseDTO(
                refreshResponse.getAccessToken(),
                refreshResponse.getTokenType()
            );

            return ResponseEntity.ok(accessTokenResponse);

        } catch (IllegalArgumentException e) { // Catch specific exceptions from the service
            // For example, if token was invalid, expired, or user not found
            // Consider a more specific custom exception from service layer
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse httpServletResponse) {

        if (refreshToken != null) {
            userService.logoutUser(refreshToken); // Call service to blacklist the token
        }

        // Instruct the browser to clear the refresh token cookie
        ResponseCookie emptyRefreshTokenCookie = ResponseCookie.from("refreshToken", "") // Empty value
                .httpOnly(true)
                .secure(refreshTokenCookieSecure) // Use configured value
                .path("/api/v1/auth") // Must match the path used when setting the cookie
                .maxAge(0) // Expire immediately
                .sameSite("Strict")
                .build();
        httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, emptyRefreshTokenCookie.toString());

        // Also, potentially clear any other session-related cookies if you have them.

        return ResponseEntity.ok("Logout successful. Please clear your access token.");
    }
    
}