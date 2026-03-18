package com.revshopproject.revshop.controller;

import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ImageController {

    /**
     * Serves product images from the product-images folder.
     * Handles files with or without extensions by probing the actual content type.
     * Supports both URL patterns since DB has mixed data:
     *   /uploads/product-images/{productId}/{filename}
     *   /product-images/{productId}/{filename}
     */
    @GetMapping({"/uploads/product-images/{productId}/{filename}", "/product-images/{productId}/{filename}"})
    public ResponseEntity<Resource> serveImage(
            @PathVariable String productId,
            @PathVariable String filename) {
        try {
            Path filePath = Paths.get("product-images", productId, filename);

            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(filePath.toUri());

            // Detect content type from the actual file bytes
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                // Fallback: try from filename
                contentType = URLConnection.guessContentTypeFromName(filename);
            }
            if (contentType == null) {
                // Last resort: try reading the file header bytes
                try {
                    contentType = URLConnection.guessContentTypeFromStream(
                            Files.newInputStream(filePath));
                } catch (IOException ignored) {}
            }
            if (contentType == null) {
                // Default to JPEG for product images
                contentType = "image/jpeg";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=86400")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
