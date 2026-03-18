package com.revshopproject.revshop.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import com.revshopproject.revshop.security.CustomLoginSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    private final CustomLoginSuccessHandler successHandler;

    public SecurityConfig(CustomLoginSuccessHandler successHandler) {
        this.successHandler = successHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Completely bypass security for uploaded images
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/uploads/**", "/product-images/**");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF is enabled by default in Spring Security. We ignore public auth endpoints
            // as they may not have an active session/CSRF token yet.
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/users/register", "/api/users/forgot-password", "/api/users/change-password"))
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    if (request.getRequestURI().startsWith("/api/")) {
                        response.sendError(401, "Unauthorized");
                    } else {
                        response.sendRedirect("/login");
                    }
                })
            )
            .authorizeHttpRequests(auth -> auth
                // 1. Static Resources & Public Pages
                .requestMatchers("/", "/login", "/register", "/forgot-password", "/product/**", "/css/**", "/js/**", "/images/**", "/uploads/**").permitAll()
                .requestMatchers("/cart", "/orders").authenticated()
                
                // 2. Public API Endpoints
                .requestMatchers("/api/users/register").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/products/**", "/api/categories/**").permitAll()

                // 3. BUYER Permissions
                .requestMatchers("/api/cart/**").hasRole("BUYER")
                .requestMatchers(HttpMethod.POST, "/api/orders/place").hasRole("BUYER")
                .requestMatchers(HttpMethod.GET, "/api/orders/my-orders").hasRole("BUYER")
                .requestMatchers(HttpMethod.POST, "/api/reviews/**").hasRole("BUYER")

                // 4. SELLER Permissions
                .requestMatchers("/api/seller/**").hasRole("SELLER")
                .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("SELLER")
                .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("SELLER")
                .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("SELLER")
                .requestMatchers(HttpMethod.POST, "/api/categories/**").hasRole("SELLER")

                // 5. Catch-all for everything else
                .anyRequest().authenticated()
            )
            // Enable standard Form Login for the browser
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/perform_login")
                .successHandler(successHandler)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessHandler((request, response, authentication) -> {
                    if (authentication != null && authentication.getName() != null) {
                        logger.info("User logged out: {}", authentication.getName());
                    }
                    response.sendRedirect("/login?logout");
                })
                .permitAll()
            );

        return http.build();
    }
}