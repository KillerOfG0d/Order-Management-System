package com.ordermanager.boundary.repository;

import com.ordermanager.entity.Item;
import com.ordermanager.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    @Query("SELECT s FROM StockMovement s WHERE s.item = ?1 AND s.remainingQuantity > 0 ORDER BY s.creationDate ASC")
    List<StockMovement> findAvailableStockByItemOrderByCreationDate(Item item);

    @Query("SELECT SUM(s.remainingQuantity) FROM StockMovement s WHERE s.item = ?1")
    Integer findTotalAvailableStockByItem(Item item);
}

