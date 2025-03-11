package com.ordermanager.boundary.repository;

import com.ordermanager.entity.Item;
import com.ordermanager.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT o FROM Order o WHERE o.completed = false AND o.item = ?1 ORDER BY o.creationDate ASC")
    List<Order> findPendingOrdersByItemOrderByCreationDate(Item item);
}

