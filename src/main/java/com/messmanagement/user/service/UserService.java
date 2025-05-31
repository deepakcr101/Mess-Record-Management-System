package com.messmanagement.user.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.messmanagement.auth.dto.LoginRequestDTO;
import com.messmanagement.auth.dto.LoginResponseDTO;
import com.messmanagement.auth.dto.TokenRefreshResponseDTO;
import com.messmanagement.user.dto.AdminCreateUserRequestDTO;
import com.messmanagement.user.dto.AdminUpdateUserRequestDTO;
import com.messmanagement.user.dto.UserRegistrationRequestDTO;
import com.messmanagement.user.dto.UserResponseDTO;

public interface UserService {
    UserResponseDTO registerStudent(UserRegistrationRequestDTO registrationRequest);

    LoginResponseDTO loginUser(LoginRequestDTO loginRequest); // New method for login

    TokenRefreshResponseDTO refreshToken(String refreshTokenValue); 

    UserResponseDTO getUserProfileByEmail(String email);

    UserResponseDTO adminCreateUser(AdminCreateUserRequestDTO createRequest); 

    Page<UserResponseDTO> getAllStudents(Pageable pageable); 
    
    UserResponseDTO getUserById(Long userId);

    UserResponseDTO adminUpdateUser(Long userId, AdminUpdateUserRequestDTO updateRequest);

    void deleteUserById(Long userId);

}