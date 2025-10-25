package com.server.services;

import com.server.Models.Product;
import com.server.Models.User;
import com.server.Repositories.ProductRepository;
import com.server.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    private String getCurrentUserEmail() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }

    private User getCurrentUser() {
        String email = getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public Product addProduct(Product product, MultipartFile imageFile) throws IOException {
        User currentUser = getCurrentUser();

        // Verify user is a farmer
        if (!currentUser.getRole().toString().equals("FARMER")) {
            throw new RuntimeException("Only farmers can add products");
        }

        // Upload image to Cloudinary
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = cloudinaryService.uploadImage(imageFile);
            product.setImageUrl(imageUrl);
        }

        product.setFarmer(currentUser);
        return productRepository.save(product);
    }

    public Product updateProduct(String productId, Product productDetails, MultipartFile imageFile) throws IOException {
        User currentUser = getCurrentUser();
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Verify the product belongs to the current farmer
        if (!existingProduct.getFarmer().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only update your own products");
        }

        // Update fields
        if (productDetails.getName() != null) {
            existingProduct.setName(productDetails.getName());
        }
        if (productDetails.getCategory() != null) {
            existingProduct.setCategory(productDetails.getCategory());
        }
        if (productDetails.getPricePerKg() != null) {
            existingProduct.setPricePerKg(productDetails.getPricePerKg());
        }
        if (productDetails.getDescription() != null) {
            existingProduct.setDescription(productDetails.getDescription());
        }

        // Update image if provided
        if (imageFile != null && !imageFile.isEmpty()) {
            // Delete old image if exists
            if (existingProduct.getImageUrl() != null) {
                cloudinaryService.deleteImage(existingProduct.getImageUrl());
            }
            // Upload new image
            String newImageUrl = cloudinaryService.uploadImage(imageFile);
            existingProduct.setImageUrl(newImageUrl);
        }

        return productRepository.save(existingProduct);
    }

    public List<Product> getMyProducts() {
        User currentUser = getCurrentUser();
        return productRepository.findByFarmerId(currentUser.getId());
    }

    public Product updateInventory(String productId, Double quantity) {
        User currentUser = getCurrentUser();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Verify the product belongs to the current farmer
        if (!product.getFarmer().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only update your own products");
        }

        product.setAvailableQuantity(quantity);
        return productRepository.save(product);
    }

    public Product getProductById(String productId) {
        User currentUser = getCurrentUser();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Verify the product belongs to the current farmer
        if (!product.getFarmer().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only access your own products");
        }

        return product;
    }
    // Add these methods to your existing ProductService class

    public Page<Product> searchProducts(String category, Double minPrice, Double maxPrice,
                                        String keyword, Pageable pageable) {

        // If keyword is provided, search by name and description
        if (keyword != null && !keyword.trim().isEmpty()) {
            return productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                    keyword, keyword, pageable);
        }

        // If category is provided, filter by category
        if (category != null && !category.trim().isEmpty()) {
            if (minPrice != null && maxPrice != null) {
                return productRepository.findByCategoryAndPricePerKgBetween(
                        category, minPrice, maxPrice, pageable);
            } else if (minPrice != null) {
                return productRepository.findByCategoryAndPricePerKgGreaterThanEqual(
                        category, minPrice, pageable);
            } else if (maxPrice != null) {
                return productRepository.findByCategoryAndPricePerKgLessThanEqual(
                        category, maxPrice, pageable);
            } else {
                return productRepository.findByCategory(category, pageable);
            }
        }

        // If only price range is provided
        if (minPrice != null && maxPrice != null) {
            return productRepository.findByPricePerKgBetween(minPrice, maxPrice, pageable);
        } else if (minPrice != null) {
            return productRepository.findByPricePerKgGreaterThanEqual(minPrice, pageable);
        } else if (maxPrice != null) {
            return productRepository.findByPricePerKgLessThanEqual(maxPrice, pageable);
        }

        // Return all products with pagination
        return productRepository.findAll(pageable);
    }

    // Add this method to your existing ProductService class
    public List<String> getAllCategories() {
        List<Product> products = productRepository.findDistinctCategoriesProjected();

        // Extract unique categories using Java Streams
        return products.stream()
                .map(Product::getCategory)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    // Alternative implementation using aggregation (more efficient)
    public List<String> getAllCategoriesAggregation() {
        // This requires a custom repository implementation
        // For now, we'll use the stream approach above
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(Product::getCategory)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}