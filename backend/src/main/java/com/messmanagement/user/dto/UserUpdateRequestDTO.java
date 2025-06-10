package com.messmanagement.user.dto;

import lombok.Data;
// Import validation annotations if you want to add specific constraints
// import jakarta.validation.constraints.Size;
// import jakarta.validation.constraints.Email; // If email was updatable by user

@Data
public class UserUpdateRequestDTO {

    private String name; // Optional: User can choose to update their name

    // @Size(min = 10, max = 15, message = "Mobile number must be between 10 and 15 digits")
    private String mobileNo; // Optional: User can choose to update their mobile number

    private String address; // Optional: User can choose to update their address

    // Note: Email is typically a unique identifier and login credential.
    // Allowing users to change their own email often requires a verification process
    // for the new email and is a more complex feature.
    // For now, we will not include email update in this DTO.
    // Password changes are also handled separately.
}