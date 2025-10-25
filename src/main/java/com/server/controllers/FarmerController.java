package com.server.controllers;

import com.server.Models.Product;
import com.server.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/farmer")
@CrossOrigin(origins = "*")
public class FarmerController {

    @Autowired
    private ProductService productService;

    @PostMapping("/products")
    public ResponseEntity<?> addProduct(
            @RequestParam("name") String name,
            @RequestParam("category") String category,
            @RequestParam("pricePerKg") Double pricePerKg,
            @RequestParam("availableQuantity") Double availableQuantity,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "language", required = false, defaultValue = "en") String language,
            @RequestParam(value = "image", required = false) MultipartFile imageFile) {

        try {
            Product product = new Product();
            product.setName(name);
            product.setCategory(category);
            product.setPricePerKg(pricePerKg);
            product.setAvailableQuantity(availableQuantity);
            product.setDescription(description);
            product.setLanguage(language);

            Product savedProduct = productService.addProduct(product, imageFile);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Product added successfully");
            response.put("product", savedProduct);

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Failed to upload image: " + e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    @PutMapping("/products/{productId}")
    public ResponseEntity<?> updateProduct(
            @PathVariable String productId,
            @RequestBody Product productDetails,
            @RequestParam(value = "image", required = false) MultipartFile imageFile) {

        try {
            Product updatedProduct = productService.updateProduct(productId, productDetails, imageFile);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Product updated successfully");
            response.put("product", updatedProduct);

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Failed to upload image: " + e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    @GetMapping("/products")
    public ResponseEntity<?> getMyProducts() {
        try {
            List<Product> products = productService.getMyProducts();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    @PatchMapping("/products/{productId}/inventory")
    public ResponseEntity<?> updateInventory(
            @PathVariable String productId,
            @RequestBody Map<String, Double> request) {

        try {
            Double quantity = request.get("quantity");
            if (quantity == null) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "Quantity is required")
                );
            }

            Product updatedProduct = productService.updateInventory(productId, quantity);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Inventory updated successfully");
            response.put("product", updatedProduct);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<?> getProduct(@PathVariable String productId) {
        try {
            Product product = productService.getProductById(productId);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }
}