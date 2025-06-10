package com.messmanagement.user.dto;

import com.messmanagement.user.entity.Role;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserResponseDTO {
    private Long userId;
    private String name;
    private String mobileNo;
    private String email;
    private String address;
    private Role role;
    private String messProvidedUserId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
