package com.revshopproject.revshop.controller;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.revshopproject.revshop.dto.ProductRequestDTO;
import com.revshopproject.revshop.dto.ProductResponseDTO;
import com.revshopproject.revshop.entity.Product;
import com.revshopproject.revshop.service.ProductService;
import com.revshopproject.revshop.utils.FileUploadUtil;

/**
 * ProductController — all DB access goes through ProductService.
 * Repositories are NOT injected here directly.
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // GET: /api/products/search?q=laptop
    @GetMapping("/search")
    public ResponseEntity<List<ProductResponseDTO>> search(@RequestParam("q") String keyword) {
        List<ProductResponseDTO> dtos = productService.searchProducts(keyword)
                .stream()
                .map(ProductResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // POST: /api/products
    @PostMapping
    public ResponseEntity<ProductResponseDTO> addProduct(@RequestBody ProductRequestDTO productRequest) {
        Product savedProduct = productService.saveProduct(productRequest);
        return ResponseEntity.ok(ProductResponseDTO.fromEntity(savedProduct));
    }

    // GET: /api/products
    @GetMapping
    public List<ProductResponseDTO> getAll() {
        return productService.getAllProducts()
                .stream()
                .map(ProductResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // GET: /api/products/category/{categoryId}
    @GetMapping("/category/{categoryId}")
    public List<ProductResponseDTO> getByCategory(@PathVariable Long categoryId) {
        return productService.getProductsByCategory(categoryId)
                .stream()
                .map(ProductResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // GET: /api/products/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(ProductResponseDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET: /api/products/{id}/images  — now uses service layer (Issue 1 fix)
    @GetMapping("/{id}/images")
    public ResponseEntity<List<String>> getProductImages(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductImages(id));
    }

    // DELETE: /api/products/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok("Product deleted successfully");
    }

    // GET: /api/products/top-rated  — now uses service layer (Issue 1 & 4 fix)
    @GetMapping("/top-rated")
    public ResponseEntity<List<ProductResponseDTO>> getTopRatedProducts() {
        List<ProductResponseDTO> dtos = productService.getTopRatedProducts(10)
                .stream()
                .map(ProductResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // GET: /api/products/homepage/top-picks  — reuses same service method (dedup fix)
    @GetMapping("/homepage/top-picks")
    public ResponseEntity<List<ProductResponseDTO>> getTopPicks() {
        List<ProductResponseDTO> dtos = productService.getTopRatedProducts(5)
                .stream()
                .map(ProductResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // POST: /api/products/{productId}/upload-images  — single endpoint handles 1 or many files (Issue 3 fix: removed upload-image)
    @PostMapping("/{productId}/upload-images")
    public ResponseEntity<String> uploadProductImages(
            @PathVariable Long productId,
            @RequestParam("images") List<MultipartFile> files) {

        try {
            StringBuilder urls = new StringBuilder();
            String uploadDir = "product-images/" + productId;

            for (MultipartFile file : files) {
                if (file.isEmpty()) continue;

                String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
                String fileName = java.util.UUID.randomUUID().toString() + "_" + originalFileName;

                FileUploadUtil.saveFile(uploadDir, fileName, file);

                String databaseUrl = "/uploads/" + uploadDir + "/" + fileName;
                productService.addImageToProduct(productId, databaseUrl);

                if (urls.length() > 0) urls.append(", ");
                urls.append(databaseUrl);
            }

            return ResponseEntity.ok("Images successfully uploaded: " + urls.toString());

        } catch (IOException e) {
            return ResponseEntity.status(500).body("Could not upload files: " + e.getMessage());
        }
    }

    // PUT: /api/products/{id}
    @org.springframework.web.bind.annotation.PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductRequestDTO productRequest) {
        Product updatedProduct = productService.updateProduct(id, productRequest);
        return ResponseEntity.ok(ProductResponseDTO.fromEntity(updatedProduct));
    }
}