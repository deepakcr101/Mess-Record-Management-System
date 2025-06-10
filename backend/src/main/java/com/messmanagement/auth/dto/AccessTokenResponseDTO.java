package com.messmanagement.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccessTokenResponseDTO {
    private String accessToken;
    private String tokenType = "Bearer";
}