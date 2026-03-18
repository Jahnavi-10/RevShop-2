package com.revshopproject.revshop.dto;

import com.revshopproject.revshop.entity.User;

/**
 * Safe DTO for returning user information — excludes sensitive fields
 * like password and security answer (Issue 8 fix).
 */
public class UserResponseDTO {

    private Long userId;
    private String name;
    private String email;
    private String role;
    private String businessName;
    private String address;

    public static UserResponseDTO fromEntity(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.userId = user.getUserId();
        dto.name = user.getName();
        dto.email = user.getEmail();
        dto.role = user.getRole();
        dto.businessName = user.getBusinessName();
        dto.address = user.getAddress();
        return dto;
    }

    public Long getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getBusinessName() { return businessName; }
    public String getAddress() { return address; }
}
