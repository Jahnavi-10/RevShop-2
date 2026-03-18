package com.revshopproject.revshop.security;

import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.revshopproject.revshop.entity.User;
import com.revshopproject.revshop.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("Failed login attempt for email: {}", email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });

        logger.info("Successful login attempt for user: {}", email);

        // Use the role from the database, typically stored as "BUYER" or "SELLER"
        // Spring Security usually requires the "ROLE_" prefix for hasRole()
        // but since we are using hasAuthority(), we can use the raw string.
        String role = user.getRole();
        if (role == null) {
            role = "BUYER"; // Fallback or throw an error based on requirements
        }

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + role))
        );
    }
}
