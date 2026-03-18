package com.revshopproject.revshop.service.impl;

import com.revshopproject.revshop.dto.ChangePasswordDTO;
import com.revshopproject.revshop.dto.ForgotPasswordDTO;
import com.revshopproject.revshop.entity.Cart;
import com.revshopproject.revshop.entity.SecurityQuestion;
import com.revshopproject.revshop.entity.User;
import com.revshopproject.revshop.repository.CartRepository;
import com.revshopproject.revshop.repository.SecurityQuestionRepository;
import com.revshopproject.revshop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private SecurityQuestionRepository questionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private SecurityQuestion testQuestion;

    @BeforeEach
    void setUp() {
        testQuestion = new SecurityQuestion();
        testQuestion.setQuestionId(1L);
        testQuestion.setQuestionText("Test Question?");

        testUser = new User();
        testUser.setUserId(1L);
        testUser.setName("John Doe");
        testUser.setEmail("john@example.com");
        testUser.setMobileNumber(9876543210L);
        testUser.setPassword("Password123!");
        testUser.setRole("BUYER");
        testUser.setSecurityQuestion(testQuestion);
        testUser.setSecurityAnswer("Test Answer");
    }

    @Test
    void testRegisterUser_Success() {
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.empty());
        when(questionRepository.findById(testQuestion.getQuestionId())).thenReturn(Optional.of(testQuestion));
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_value");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(cartRepository.save(any(Cart.class))).thenReturn(new Cart());

        User registeredUser = userService.registerUser(testUser);

        assertNotNull(registeredUser);
        verify(passwordEncoder, times(2)).encode(anyString()); // Once for password, once for answer
        verify(userRepository, times(1)).save(testUser);
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void testRegisterUser_DuplicateEmail() {
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.registerUser(testUser);
        });

        assertTrue(exception.getMessage().contains("Email already registered"));
        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    void testChangePassword_Success() {
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setEmail("john@example.com");
        dto.setOldPassword("OldPassword");
        dto.setNewPassword("NewPassword");

        User dbUser = new User();
        dbUser.setPassword("hashed_old_password");

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(dbUser));
        when(passwordEncoder.matches("OldPassword", "hashed_old_password")).thenReturn(true);
        when(passwordEncoder.encode("NewPassword")).thenReturn("hashed_new_password");
        when(userRepository.save(dbUser)).thenReturn(dbUser);

        userService.changePassword(dto);

        assertEquals("hashed_new_password", dbUser.getPassword());
        verify(userRepository, times(1)).save(dbUser);
    }

    @Test
    void testForgotPassword_Success() {
        ForgotPasswordDTO dto = new ForgotPasswordDTO();
        dto.setEmail("john@example.com");
        dto.setSecurityQuestionId(1L);
        dto.setSecurityAnswer("Test Answer");
        dto.setNewPassword("NewPassword");

        User dbUser = new User();
        dbUser.setSecurityQuestion(testQuestion);
        dbUser.setSecurityAnswer("hashed_answer");

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(dbUser));
        when(passwordEncoder.matches("Test Answer", "hashed_answer")).thenReturn(true);
        when(passwordEncoder.encode("NewPassword")).thenReturn("hashed_new_password");
        when(userRepository.save(dbUser)).thenReturn(dbUser);

        userService.forgotPassword(dto);

        assertEquals("hashed_new_password", dbUser.getPassword());
        verify(userRepository, times(1)).save(dbUser);
    }
}
