package com.server.Repositories;

import com.server.Models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    List<Product> findByFarmerId(String farmerId);

    // Search methods
    Page<Product> findByCategory(String category, Pageable pageable);
    Page<Product> findByPricePerKgBetween(Double minPrice, Double maxPrice, Pageable pageable);
    Page<Product> findByPricePerKgGreaterThanEqual(Double minPrice, Pageable pageable);
    Page<Product> findByPricePerKgLessThanEqual(Double maxPrice, Pageable pageable);
    Page<Product> findByCategoryAndPricePerKgBetween(String category, Double minPrice, Double maxPrice, Pageable pageable);
    Page<Product> findByCategoryAndPricePerKgGreaterThanEqual(String category, Double minPrice, Pageable pageable);
    Page<Product> findByCategoryAndPricePerKgLessThanEqual(String category, Double maxPrice, Pageable pageable);

    // Text search
    @Query("{ '$or': [ { 'name': { '$regex': ?0, '$options': 'i' } }, { 'description': { '$regex': ?1, '$options': 'i' } } ] }")
    Page<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description, Pageable pageable);

    // Fixed: Get distinct categories
    @Query(value = "{}", fields = "{'category' : 1}")
    List<Product> findDistinctCategoriesProjected();

    // Alternative method using aggregation
    @Query(value = "{}", sort = "{'category' : 1}")
    List<Product> findAllGroupedByCategory();
}