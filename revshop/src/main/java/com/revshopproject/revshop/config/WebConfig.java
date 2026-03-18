package com.revshopproject.revshop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    // Image serving is handled by ImageController
    // which detects content type from file bytes
}
