package com.messmanagement.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenRefreshResponseDTO {
    private String accessToken;
    private String newRefreshToken;
    private String tokenType = "Bearer";
}