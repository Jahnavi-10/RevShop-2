package com.revshopproject.revshop.service.impl;

import com.revshopproject.revshop.dto.ProductRequestDTO;
import com.revshopproject.revshop.entity.Category;
import com.revshopproject.revshop.entity.Product;
import com.revshopproject.revshop.entity.ProductImage;
import com.revshopproject.revshop.entity.User;
import com.revshopproject.revshop.repository.CategoryRepository;
import com.revshopproject.revshop.repository.ProductImageRepository;
import com.revshopproject.revshop.repository.ProductRepository;
import com.revshopproject.revshop.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;
    
    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private ProductServiceImpl productService;

    private User testSeller;
    private User testBuyer;
    private Product testProduct;
    private Category testCategory;
    private ProductRequestDTO productRequestDTO;

    @BeforeEach
    void setUp() {
        testSeller = new User();
        testSeller.setUserId(2L);
        testSeller.setRole("SELLER");
        testSeller.setEmail("seller@example.com");

        testBuyer = new User();
        testBuyer.setUserId(3L);
        testBuyer.setRole("BUYER");

        testCategory = new Category();
        testCategory.setCategoryId(10L);
        testCategory.setName("Electronics");

        testProduct = new Product();
        testProduct.setProductId(100L);
        testProduct.setName("Test Product");
        testProduct.setSeller(testSeller);
        testProduct.setCategory(testCategory);
        testProduct.setPrice(new BigDecimal("90.00"));
        testProduct.setMrp(new BigDecimal("100.00"));
        testProduct.setStock(20);
        testProduct.setInventoryThreshold(5);

        productRequestDTO = new ProductRequestDTO();
        productRequestDTO.setName("Test Product");
        productRequestDTO.setPrice(new BigDecimal("90.00"));
        productRequestDTO.setMrp(new BigDecimal("100.00"));
        productRequestDTO.setStock(20);
        productRequestDTO.setInventoryThreshold(5);
        productRequestDTO.setCategoryId(10L);
    }

    @Test
    void testSaveProduct_Success() {
        when(userService.getCurrentUser()).thenReturn(testSeller);
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(testCategory));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Product savedProduct = productService.saveProduct(productRequestDTO);

        assertNotNull(savedProduct);
        assertEquals("Test Product", savedProduct.getName());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testSaveProduct_NotSeller_ThrowsException() {
        when(userService.getCurrentUser()).thenReturn(testBuyer);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productService.saveProduct(productRequestDTO);
        });

        assertTrue(exception.getMessage().contains("Access Denied"));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void testSaveProduct_InvalidPrice_ThrowsException() {
        when(userService.getCurrentUser()).thenReturn(testSeller);
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(testCategory));

        // Price > MRP
        productRequestDTO.setPrice(new BigDecimal("110.00"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productService.saveProduct(productRequestDTO);
        });

        assertTrue(exception.getMessage().contains("Selling price must be strictly lower"));
    }
    
    @Test
    void testSaveProduct_InvalidStock_ThrowsException() {
        when(userService.getCurrentUser()).thenReturn(testSeller);
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(testCategory));

        // Stock < Threshold
        productRequestDTO.setStock(2);
        productRequestDTO.setInventoryThreshold(5);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productService.saveProduct(productRequestDTO);
        });

        assertTrue(exception.getMessage().contains("Initial stock must be greater"));
    }

    @Test
    void testGetAllProducts() {
        when(productRepository.findAll()).thenReturn(Arrays.asList(testProduct));

        List<Product> products = productService.getAllProducts();

        assertEquals(1, products.size());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void testGetProductById() {
        when(productRepository.findById(testProduct.getProductId())).thenReturn(Optional.of(testProduct));

        Optional<Product> found = productService.getProductById(testProduct.getProductId());

        assertTrue(found.isPresent());
        verify(productRepository, times(1)).findById(testProduct.getProductId());
    }

    @Test
    void testDeleteProduct_Success() {
        when(userService.getCurrentUser()).thenReturn(testSeller);
        when(productRepository.findById(testProduct.getProductId())).thenReturn(Optional.of(testProduct));

        productService.deleteProduct(testProduct.getProductId());

        verify(productRepository, times(1)).delete(testProduct);
    }
    
    @Test
    void testDeleteProduct_Unauthorized_ThrowsException() {
        // Buyer trying to delete
        when(userService.getCurrentUser()).thenReturn(testBuyer);
        when(productRepository.findById(testProduct.getProductId())).thenReturn(Optional.of(testProduct));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productService.deleteProduct(testProduct.getProductId());
        });

        assertTrue(exception.getMessage().contains("Unauthorized"));
        verify(productRepository, never()).delete(any(Product.class));
    }

    @Test
    void testUpdateProduct_Success() {
        when(userService.getCurrentUser()).thenReturn(testSeller);
        when(productRepository.findById(testProduct.getProductId())).thenReturn(Optional.of(testProduct));
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(testCategory));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        ProductRequestDTO updateDTO = new ProductRequestDTO();
        updateDTO.setName("Updated Name");
        updateDTO.setPrice(new BigDecimal("80.00"));
        updateDTO.setMrp(new BigDecimal("100.00"));
        updateDTO.setStock(30);
        updateDTO.setCategoryId(10L);

        Product updatedProduct = productService.updateProduct(testProduct.getProductId(), updateDTO);

        assertNotNull(updatedProduct);
        assertEquals("Updated Name", testProduct.getName()); // Object gets modified
        assertEquals(new BigDecimal("80.00"), testProduct.getPrice());
        verify(productRepository, times(1)).save(testProduct);
    }
    
    @Test
    void testAddImageToProduct_Success() {
        when(userService.getCurrentUser()).thenReturn(testSeller);
        when(productRepository.findById(testProduct.getProductId())).thenReturn(Optional.of(testProduct));
        when(productImageRepository.save(any(ProductImage.class))).thenReturn(new ProductImage());

        productService.addImageToProduct(testProduct.getProductId(), "image.url");

        verify(productImageRepository, times(1)).save(any(ProductImage.class));
    }
}
