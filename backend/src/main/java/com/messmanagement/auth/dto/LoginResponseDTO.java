package com.messmanagement.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    private String accessToken;
    private String refreshToken; // The plan suggests refresh token via HttpOnly cookie,
                                // but DTO might still carry it or a confirmation.
                                // For now, let's include it. We'll refine token delivery later.
    private String tokenType = "Bearer";
    private Long userId;
    private String email;
    private String role;
}
