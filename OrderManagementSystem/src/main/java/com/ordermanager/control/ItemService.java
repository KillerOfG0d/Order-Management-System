package com.ordermanager.control;

import com.ordermanager.boundary.dto.ItemDTO;
import com.ordermanager.boundary.repository.ItemRepository;
import com.ordermanager.entity.Item;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemService {
    private static final Logger logger = LogManager.getLogger(ItemService.class);

    @Autowired
    private ItemRepository itemRepository;

    @Transactional(readOnly = true)
    public List<ItemDTO> getAllItems() {
        return itemRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ItemDTO getItemById(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item not found with id: " + id));
        return convertToDTO(item);
    }

    @Transactional
    public ItemDTO createItem(ItemDTO itemDTO) {
        Item item = new Item();
        item.setName(itemDTO.getName());

        Item savedItem = itemRepository.save(item);
        logger.info("Created new item: {}", savedItem.getId());
        return convertToDTO(savedItem);
    }

    @Transactional
    public ItemDTO updateItem(Long id, ItemDTO itemDTO) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item not found with id: " + id));

        item.setName(itemDTO.getName());
        Item updatedItem = itemRepository.save(item);
        logger.info("Updated item: {}", updatedItem.getId());
        return convertToDTO(updatedItem);
    }

    @Transactional
    public void deleteItem(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item not found with id: " + id));

        itemRepository.delete(item);
        logger.info("Deleted item: {}", id);
    }

    private ItemDTO convertToDTO(Item item) {
        ItemDTO dto = new ItemDTO();
        dto.setId(item.getId());
        dto.setName(item.getName());
        return dto;
    }

    public Item getItemEntityById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item not found with id: " + id));
    }
}
