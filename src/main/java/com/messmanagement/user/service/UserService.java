package com.messmanagement.user.service;

import com.messmanagement.user.dto.UserRegistrationRequestDTO;
import com.messmanagement.user.dto.UserResponseDTO;

public interface UserService {
    UserResponseDTO registerStudent(UserRegistrationRequestDTO registrationRequest);
    // Other user-related methods will be added here later (e.g., login, findUserById, etc.)
}
