package com.messmanagement.user.dto;

import lombok.Data;
// Import validation annotations if you've added the spring-boot-starter-validation dependency
// import jakarta.validation.constraints.Email;
// import jakarta.validation.constraints.NotBlank;
// import jakarta.validation.constraints.Size;

@Data
public class UserRegistrationRequestDTO {

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

    // messProvidedUserId might be optional at registration for students,
    // and potentially set by an admin later, or required if self-registration must include it.
    // For now, let's make it optional in the DTO.
    private String messProvidedUserId;
}
