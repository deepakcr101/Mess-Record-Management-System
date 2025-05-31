package com.messmanagement.auth.dto;

import lombok.Data;
// import jakarta.validation.constraints.Email;
// import jakarta.validation.constraints.NotBlank;

@Data
public class LoginRequestDTO {

    // @NotBlank(message = "Email cannot be blank")
    // @Email(message = "Email should be valid")
    private String email;

    // @NotBlank(message = "Password cannot be blank")
    private String password;
}
