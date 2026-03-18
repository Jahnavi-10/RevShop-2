package com.revshopproject.revshop.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.revshopproject.revshop.dto.ChangePasswordDTO;
import com.revshopproject.revshop.dto.ForgotPasswordDTO;
import com.revshopproject.revshop.dto.UserRegistrationDTO;
import com.revshopproject.revshop.dto.UserResponseDTO;
import com.revshopproject.revshop.entity.SecurityQuestion;
import com.revshopproject.revshop.entity.User;
import com.revshopproject.revshop.service.UserService;

/**
 * UserController — changes:
 *  - Issue 8: Returns UserResponseDTO (not raw User entity) from /register
 *  - Issue 9: Removed dead /login endpoint (Spring Security handles auth via /perform_login)
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // POST: /api/users/register
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegistrationDTO dto) {
        if (dto.getPassword() == null || !dto.getPassword().equals(dto.getConfirmPassword())) {
            return ResponseEntity.badRequest().body("Password and confirm password do not match");
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setMobileNumber(dto.getMobileNumber());
        user.setPassword(dto.getPassword());
        // Security: Only allow BUYER and SELLER registrations
        if ("SELLER".equalsIgnoreCase(dto.getRole())) {
            user.setRole("SELLER");
        } else {
            user.setRole("BUYER");
        }
        user.setBusinessName(dto.getBusinessName());
        user.setAddress(dto.getAddress());
        user.setSecurityAnswer(dto.getSecurityAnswer());

        if (dto.getSecurityQuestionId() != null) {
            SecurityQuestion sq = new SecurityQuestion();
            sq.setQuestionId(dto.getSecurityQuestionId());
            user.setSecurityQuestion(sq);
        }

        User savedUser = userService.registerUser(user);
        // Return safe DTO — never expose the raw entity with password hash
        return ResponseEntity.ok(UserResponseDTO.fromEntity(savedUser));
    }

    // POST: /api/users/change-password
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordDTO dto) {
        if (dto.getNewPassword() == null || !dto.getNewPassword().equals(dto.getConfirmPassword())) {
            return ResponseEntity.badRequest().body("New password and confirm password do not match");
        }
        User user = userService.changePassword(dto);
        return ResponseEntity.ok(UserResponseDTO.fromEntity(user));
    }

    // POST: /api/users/forgot-password
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordDTO dto) {
        if (dto.getNewPassword() == null || !dto.getNewPassword().equals(dto.getConfirmPassword())) {
            return ResponseEntity.badRequest().body("New password and confirm password do not match");
        }
        User user = userService.forgotPassword(dto);
        return ResponseEntity.ok(UserResponseDTO.fromEntity(user));
    }
}