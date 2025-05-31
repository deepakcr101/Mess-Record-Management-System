package com.messmanagement.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginSuccessBodyResponseDTO {
    private String accessToken;
    private String tokenType = "Bearer";
    private Long userId;
    private String email;
    private String role;
}