package com.messmanagement.user.service;

import com.messmanagement.auth.dto.LoginRequestDTO;
import com.messmanagement.auth.dto.LoginResponseDTO;
import com.messmanagement.user.dto.UserRegistrationRequestDTO;
import com.messmanagement.user.dto.UserResponseDTO;

public interface UserService {
    UserResponseDTO registerStudent(UserRegistrationRequestDTO registrationRequest);

    LoginResponseDTO loginUser(LoginRequestDTO loginRequest); // New method for login
}