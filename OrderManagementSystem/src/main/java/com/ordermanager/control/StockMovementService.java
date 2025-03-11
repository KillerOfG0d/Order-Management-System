package com.ordermanager.control;

import com.ordermanager.boundary.dto.StockMovementDTO;
import com.ordermanager.boundary.repository.StockMovementRepository;
import com.ordermanager.entity.Item;
import com.ordermanager.entity.StockMovement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StockMovementService {
    private static final Logger logger = LogManager.getLogger(StockMovementService.class);

    @Autowired
    private StockMovementRepository stockMovementRepository;

    @Autowired
    private ItemService itemService;

    @Autowired
    private OrderService orderService;

    @Transactional(readOnly = true)
    public List<StockMovementDTO> getAllStockMovements() {
        return stockMovementRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StockMovementDTO getStockMovementById(Long id) {
        StockMovement stockMovement = stockMovementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Stock movement not found with id: " + id));
        return convertToDTO(stockMovement);
    }

    @Transactional
    public StockMovementDTO createStockMovement(StockMovementDTO stockMovementDTO) {
        StockMovement stockMovement = new StockMovement();
        stockMovement.setCreationDate(LocalDateTime.now());

        Item item = itemService.getItemEntityById(stockMovementDTO.getItemId());
        stockMovement.setItem(item);

        stockMovement.setQuantity(stockMovementDTO.getQuantity());
        stockMovement.setRemainingQuantity(stockMovementDTO.getQuantity());

        StockMovement savedStockMovement = stockMovementRepository.save(stockMovement);
        logger.info("Created new stock movement: {}", savedStockMovement.getId());

        // Try to fulfill pending orders with this new stock
        orderService.tryFulfillPendingOrders(item);

        return convertToDTO(savedStockMovement);
    }

    @Transactional
    public StockMovementDTO updateStockMovement(Long id, StockMovementDTO stockMovementDTO) {
        StockMovement stockMovement = stockMovementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Stock movement not found with id: " + id));

        Item item = itemService.getItemEntityById(stockMovementDTO.getItemId());
        stockMovement.setItem(item);

        // Only allow updating the quantity if it's greater than what has been used
        int usedQuantity = stockMovement.getQuantity() - stockMovement.getRemainingQuantity();
        if (stockMovementDTO.getQuantity() < usedQuantity) {
            throw new IllegalArgumentException("Cannot reduce quantity below the amount already used");
        }

        stockMovement.setQuantity(stockMovementDTO.getQuantity());
        stockMovement.setRemainingQuantity(stockMovementDTO.getQuantity() - usedQuantity);

        StockMovement updatedStockMovement = stockMovementRepository.save(stockMovement);
        logger.info("Updated stock movement: {}", updatedStockMovement.getId());

        // If quantity increased, try to fulfill pending orders
        orderService.tryFulfillPendingOrders(item);

        return convertToDTO(updatedStockMovement);
    }

    @Transactional
    public void deleteStockMovement(Long id) {
        StockMovement stockMovement = stockMovementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Stock movement not found with id: " + id));

        // Only allow deletion if the stock movement hasn't been used
        if (stockMovement.getQuantity() > stockMovement.getRemainingQuantity()) {
            throw new IllegalArgumentException("Cannot delete stock movement that has been partially or fully used");
        }

        stockMovementRepository.delete(stockMovement);
        logger.info("Deleted stock movement: {}", id);
    }

    private StockMovementDTO convertToDTO(StockMovement stockMovement) {
        StockMovementDTO dto = new StockMovementDTO();
        dto.setId(stockMovement.getId());
        dto.setCreationDate(stockMovement.getCreationDate());
        dto.setItemId(stockMovement.getItem().getId());
        dto.setItemName(stockMovement.getItem().getName());
        dto.setQuantity(stockMovement.getQuantity());
        dto.setRemainingQuantity(stockMovement.getRemainingQuantity());

        List<Long> orderIds = stockMovement.getOrders().stream()
                .map(order -> order.getId())
                .collect(Collectors.toList());
        dto.setOrderIds(orderIds);

        return dto;
    }
}