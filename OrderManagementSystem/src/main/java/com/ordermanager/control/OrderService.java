package com.ordermanager.control;

import com.ordermanager.boundary.dto.OrderDTO;
import com.ordermanager.boundary.repository.OrderRepository;
import com.ordermanager.boundary.repository.StockMovementRepository;
import com.ordermanager.entity.Item;
import com.ordermanager.entity.Order;
import com.ordermanager.entity.StockMovement;
import com.ordermanager.entity.User;
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
public class OrderService {
    private static final Logger logger = LogManager.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private StockMovementRepository stockMovementRepository;

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Transactional(readOnly = true)
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));
        return convertToDTO(order);
    }

    @Transactional
    public OrderDTO createOrder(OrderDTO orderDTO) {
        Order order = new Order();
        order.setCreationDate(LocalDateTime.now());

        Item item = itemService.getItemEntityById(orderDTO.getItemId());
        order.setItem(item);

        User user = userService.getUserEntityById(orderDTO.getUserId());
        order.setUser(user);

        order.setQuantity(orderDTO.getQuantity());
        order.setFulfilledQuantity(0);
        order.setCompleted(false);

        Order savedOrder = orderRepository.save(order);
        logger.info("Created new order: {}", savedOrder.getId());

        // Try to fulfill the order with existing stock
        tryFulfillOrder(savedOrder);

        return convertToDTO(savedOrder);
    }

    @Transactional
    public OrderDTO updateOrder(Long id, OrderDTO orderDTO) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));

        Item item = itemService.getItemEntityById(orderDTO.getItemId());
        order.setItem(item);

        User user = userService.getUserEntityById(orderDTO.getUserId());
        order.setUser(user);

        order.setQuantity(orderDTO.getQuantity());

        Order updatedOrder = orderRepository.save(order);
        logger.info("Updated order: {}", updatedOrder.getId());

        // If quantity changed, try to fulfill the order
        if (order.getQuantity() > order.getFulfilledQuantity()) {
            tryFulfillOrder(order);
        }

        return convertToDTO(updatedOrder);
    }

    @Transactional
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));

        orderRepository.delete(order);
        logger.info("Deleted order: {}", id);
    }

    @Transactional
    public void tryFulfillOrder(Order order) {
        if (order.isCompleted()) {
            return;
        }

        int remainingToFulfill = order.getQuantity() - order.getFulfilledQuantity();
        if (remainingToFulfill <= 0) {
            return;
        }

        List<StockMovement> availableStock = stockMovementRepository.findAvailableStockByItemOrderByCreationDate(order.getItem());

        for (StockMovement stockMovement : availableStock) {
            if (remainingToFulfill <= 0) {
                break;
            }

            int toUse = Math.min(remainingToFulfill, stockMovement.getRemainingQuantity());

            stockMovement.setRemainingQuantity(stockMovement.getRemainingQuantity() - toUse);
            stockMovementRepository.save(stockMovement);

            order.setFulfilledQuantity(order.getFulfilledQuantity() + toUse);
            order.addStockMovement(stockMovement);
            stockMovement.addOrder(order);

            remainingToFulfill -= toUse;

            logger.info("Used {} units from stock movement {} for order {}",
                    toUse, stockMovement.getId(), order.getId());
        }

        if (order.getFulfilledQuantity() >= order.getQuantity()) {
            order.setCompleted(true);
            logger.info("Order completed: {}", order.getId());

            // Send email notification
            emailService.sendOrderCompletionEmail(order);
        }

        orderRepository.save(order);
    }

    @Transactional
    public void tryFulfillPendingOrders(Item item) {
        List<Order> pendingOrders = orderRepository.findPendingOrdersByItemOrderByCreationDate(item);

        for (Order order : pendingOrders) {
            tryFulfillOrder(order);
        }
    }

    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setCreationDate(order.getCreationDate());
        dto.setItemId(order.getItem().getId());
        dto.setItemName(order.getItem().getName());
        dto.setQuantity(order.getQuantity());
        dto.setFulfilledQuantity(order.getFulfilledQuantity());
        dto.setUserId(order.getUser().getId());
        dto.setUserName(order.getUser().getName());
        dto.setCompleted(order.isCompleted());
        dto.setCompletionPercentage(order.getCompletionPercentage());

        List<Long> stockMovementIds = order.getStockMovements().stream()
                .map(StockMovement::getId)
                .collect(Collectors.toList());
        dto.setStockMovementIds(stockMovementIds);

        return dto;
    }
}
