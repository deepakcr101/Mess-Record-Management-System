package com.messmanagement.user.dto;

import com.messmanagement.user.entity.Role; // If admin can set role

import lombok.Data;
// Import validation annotations if needed
// import jakarta.validation.constraints.Email;
// import jakarta.validation.constraints.NotBlank;
// import jakarta.validation.constraints.NotNull;
// import jakarta.validation.constraints.Size;

@Data
public class AdminCreateUserRequestDTO {

    // @NotBlank(message = "Name cannot be blank")
    private String name;

    // @NotBlank(message = "Mobile number cannot be blank")
    // @Size(min = 10, max = 15, message = "Mobile number must be between 10 and 15 digits")
    private String mobileNo;

    // @NotBlank(message = "Email cannot be blank")
    // @Email(message = "Email should be valid")
    private String email;

    // @NotBlank(message = "Address cannot be blank")
    private String address;

    // @NotBlank(message = "Password cannot be blank")
    // @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    // Admin can provide this directly as per plan (source 74)
    private String messProvidedUserId;

    // Optional: Admin might be able to set the role, defaulting to STUDENT if not provided
    // @NotNull(message = "Role cannot be null")
    private Role role = Role.STUDENT; // Default to STUDENT
}