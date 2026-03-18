package com.revshopproject.revshop.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.revshopproject.revshop.dto.ProductRequestDTO;
import com.revshopproject.revshop.entity.Category;
import com.revshopproject.revshop.entity.Product;
import com.revshopproject.revshop.entity.ProductImage;
import com.revshopproject.revshop.entity.User;
import com.revshopproject.revshop.repository.CategoryRepository;
import com.revshopproject.revshop.repository.ProductImageRepository;
import com.revshopproject.revshop.repository.ProductRepository;
import com.revshopproject.revshop.service.ProductService;
import com.revshopproject.revshop.service.UserService;

import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final UserService userService;

    public ProductServiceImpl(ProductRepository productRepository, 
                              CategoryRepository categoryRepository,
                              ProductImageRepository productImageRepository,
                              UserService userService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productImageRepository = productImageRepository;
        this.userService = userService;
    }

    @Override
    @Transactional
    public Product saveProduct(ProductRequestDTO dto) {
        User seller = userService.getCurrentUser();

        // VALIDATION LOGIC
        if (!"SELLER".equalsIgnoreCase(seller.getRole())) {
            throw new RuntimeException("Access Denied: Only users with the 'SELLER' role can add products.");
        }

        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setMrp(dto.getMrp());
        product.setStock(dto.getStock());
        product.setInventoryThreshold(dto.getInventoryThreshold());
        product.setSeller(seller);

        // 3. Fetch and Validate Category (Prevent Transient Error)
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found."));
            product.setCategory(category);
        }

        // 4. Business Rules: Price & Stock
        if (product.getPrice().compareTo(product.getMrp()) >= 0) {
            logger.warn("Validation failed for product {}: Selling price {} is not lower than MRP {}", product.getName(), product.getPrice(), product.getMrp());
            throw new RuntimeException("Validation Error: Selling price must be strictly lower than the MRP.");
        }
        
        int threshold = (product.getInventoryThreshold() != null) ? product.getInventoryThreshold() : 0;
        if (product.getStock() <= threshold) {
            logger.warn("Validation failed for product {}: Stock {} is not above the threshold {}", product.getName(), product.getStock(), threshold);
            throw new RuntimeException("Validation Error: Initial stock must be greater than the inventory threshold (" + threshold + ").");
        }

        Product savedProduct = productRepository.save(product);
        logger.info("New product '{}' created by seller: {}", savedProduct.getName(), seller.getEmail());
        return savedProduct;
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategory_CategoryId(categoryId);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        User currentUser = userService.getCurrentUser();
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        // Ownership Check
        if (!product.getSeller().getUserId().equals(currentUser.getUserId())) {
            throw new RuntimeException("Unauthorized: You can only delete your own products.");
        }
        
        logger.info("Product '{}' (ID: {}) deleted by seller: {}", product.getName(), id, currentUser.getEmail());
        productRepository.delete(product);
    }

    @Override
    public List<Product> searchProducts(String keyword) {
        return productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword);
    }

    @Override
    public List<Product> getProductsBySellerId(Long sellerId) {
        return productRepository.findBySeller_UserId(sellerId);
    }
    
    @Override
    @Transactional
    public void addImageToProduct(Long productId, String imageUrl) {
        User currentUser = userService.getCurrentUser();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Ownership Check
        if (!product.getSeller().getUserId().equals(currentUser.getUserId())) {
            throw new RuntimeException("Unauthorized: You can only add images to your own products.");
        }

        ProductImage productImage = new ProductImage();
        productImage.setProduct(product);
        productImage.setImageUrl(imageUrl);
        
        productImageRepository.save(productImage);
    }

    @Override
    @Transactional
    public Product updateProduct(Long id, ProductRequestDTO dto) {
        User seller = userService.getCurrentUser();
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Ownership Check
        if (!product.getSeller().getUserId().equals(seller.getUserId())) {
            throw new RuntimeException("Unauthorized: You can only update your own products.");
        }

        // VALIDATION LOGIC
        if (dto.getName() != null) product.setName(dto.getName());
        if (dto.getDescription() != null) product.setDescription(dto.getDescription());
        if (dto.getPrice() != null) product.setPrice(dto.getPrice());
        if (dto.getMrp() != null) product.setMrp(dto.getMrp());
        if (dto.getStock() != null) product.setStock(dto.getStock());
        if (dto.getInventoryThreshold() != null) product.setInventoryThreshold(dto.getInventoryThreshold());

        // 3. Update Category if provided
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found."));
            product.setCategory(category);
        }

        // 4. Business Rules: Price & Stock
        if (product.getPrice().compareTo(product.getMrp()) >= 0) {
            logger.warn("Validation failed during update of product {}: Selling price {} is not lower than MRP {}", product.getName(), product.getPrice(), product.getMrp());
            throw new RuntimeException("Validation Error: Selling price must be strictly lower than the MRP.");
        }
        
        int threshold = (product.getInventoryThreshold() != null) ? product.getInventoryThreshold() : 0;
        if (product.getStock() <= threshold) {
            logger.warn("Validation failed during update of product {}: Stock {} is not above the threshold {}", product.getName(), product.getStock(), threshold);
            throw new RuntimeException("Validation Error: Stock must be greater than the inventory threshold (" + threshold + ").");
        }

        Product updatedProduct = productRepository.save(product);
        logger.info("Product '{}' (ID: {}) updated by seller: {}. New stock level: {}", 
            updatedProduct.getName(), id, seller.getEmail(), updatedProduct.getStock());
        
        if (updatedProduct.getStock() <= threshold) {
            logger.warn("Low Stock Warning for Product '{}' (ID: {}). Current stock: {}", 
                updatedProduct.getName(), id, updatedProduct.getStock());
        }

        return updatedProduct;
    }

    @Override
    public List<String> getProductImages(Long productId) {
        return productImageRepository.findByProduct_ProductId(productId)
                .stream()
                .map(ProductImage::getImageUrl)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<Product> getTopRatedProducts(int limit) {
        return productRepository.findTopRatedProducts()
                .stream()
                .limit(limit)
                .collect(java.util.stream.Collectors.toList());
    }
}