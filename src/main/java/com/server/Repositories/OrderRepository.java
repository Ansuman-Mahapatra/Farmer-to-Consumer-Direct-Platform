package com.server.Repositories;

import com.server.Models.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findByConsumerIdOrderByOrderDateDesc(String consumerId);
    List<Order> findByStatus(String status);
}