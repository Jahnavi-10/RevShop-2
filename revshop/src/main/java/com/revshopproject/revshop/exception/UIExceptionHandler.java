package com.revshopproject.revshop.exception;

import com.revshopproject.revshop.controller.PageController;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice(assignableTypes = {PageController.class})
@Order(1) // Priority over GlobalExceptionHandler for PageController
public class UIExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(UIExceptionHandler.class);

    @ExceptionHandler(NoResourceFoundException.class)
    public ModelAndView handle404(HttpServletRequest request, Exception ex) {
        logger.error("404 Error: URL {} not found. Message: {}", request.getRequestURL(), ex.getMessage());
        return new ModelAndView("error/404");
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handle500(HttpServletRequest request, Exception ex) {
        logger.error("500 Error: Exception requested from URL {}", request.getRequestURL(), ex);
        return new ModelAndView("error/500");
    }
}
