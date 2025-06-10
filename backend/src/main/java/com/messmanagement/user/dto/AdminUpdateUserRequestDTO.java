package com.messmanagement.user.dto;

import com.messmanagement.user.entity.Role;

import lombok.Data;
// import jakarta.validation.constraints.Email;
// import jakarta.validation.constraints.Size;
// Potentially add more specific validation if needed

@Data
public class AdminUpdateUserRequestDTO {
    // Fields that admin can update. Make them optional.
    private String name;

    // @Size(min = 10, max = 15, message = "Mobile number must be between 10 and 15 digits")
    private String mobileNo;

    // @Email(message = "Email should be valid")
    // Note: Changing email might have implications (e.g., uniqueness, re-verification)
    private String email;

    private String address;

    private String messProvidedUserId;

    private Role role; // Admin might be able to change a user's role

    // Password update is often a separate, dedicated endpoint for security/audit reasons.
    // If password can be updated here, add:
    // private String password;
    // And handle its encoding in the service. For now, let's assume password is changed via a different flow.
}