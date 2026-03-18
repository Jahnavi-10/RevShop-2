package com.revshopproject.revshop.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.revshopproject.revshop.dto.ProductResponseDTO;
import com.revshopproject.revshop.entity.Product;
import com.revshopproject.revshop.service.ProductService;
import com.revshopproject.revshop.service.UserService;

/**
 * PageController — now uses constructor injection (was @Autowired field injection).
 * SecurityQuestionRepository no longer injected directly here; uses UserService instead.
 */
@Controller
public class PageController {

    private final ProductService productService;
    private final UserService userService;

    public PageController(ProductService productService, UserService userService) {
        this.productService = productService;
        this.userService = userService;
    }

    @GetMapping("/")
    public String showIndexPage(Model model) {
        List<Product> products = productService.getAllProducts();
        List<ProductResponseDTO> productDTOs = products.stream()
                .map(ProductResponseDTO::fromEntity)
                .collect(Collectors.toList());
        model.addAttribute("products", productDTOs);
        return "index";
    }

    @GetMapping("/login")
    public String renderLoginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String renderRegisterPage(Model model) {
        model.addAttribute("securityQuestions", userService.getSecurityQuestions());
        return "register";
    }

    @GetMapping("/forgot-password")
    public String renderForgotPasswordPage(Model model) {
        model.addAttribute("securityQuestions", userService.getSecurityQuestions());
        return "forgot-password";
    }

    @GetMapping("/seller/dashboard")
    public String renderSellerDashboard() {
        return "seller/dashboard";
    }

    @GetMapping("/cart")
    public String renderCartPage() {
        return "cart";
    }

    @GetMapping("/change-password")
    public String renderChangePasswordPage(Model model) {
        model.addAttribute("securityQuestions", userService.getSecurityQuestions());
        return "change-password";
    }

    @GetMapping("/orders")
    public String renderOrdersPage() {
        return "orders";
    }

    @GetMapping("/notifications")
    public String renderNotificationsPage() {
        return "notifications";
    }

    @GetMapping("/favorites")
    public String renderFavoritesPage() {
        return "favorites";
    }

    @GetMapping("/product/{id}")
    public String renderProductDetailPage() {
        return "product-detail";
    }
}