package com.cropplanner.inventory;

import com.cropplanner.exception.BusinessRuleException;
import com.cropplanner.exception.ResourceNotFoundException;
import com.cropplanner.inventory.InventoryDTOs.CreateInventoryItemRequest;
import com.cropplanner.inventory.InventoryDTOs.InventoryItemResponse;
import com.cropplanner.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryItemRepository inventoryItemRepository;

    @Transactional
    public InventoryItemResponse create(User user, CreateInventoryItemRequest req) {
        InventoryItem item = InventoryItem.builder()
                .user(user)
                .type(req.getType())
                .name(req.getName().trim())
                .quantity(req.getQuantity())
                .unit(req.getUnit().trim())
                .lowStockThreshold(req.getLowStockThreshold())
                .notes(req.getNotes())
                .build();

        InventoryItem saved = inventoryItemRepository.save(item);
        log.info("Inventory item created — user: {} type: {} name: {}", user.getEmail(), req.getType(), req.getName());
        return InventoryItemResponse.from(saved);
    }

    public List<InventoryItemResponse> getForUser(User user) {
        return inventoryItemRepository.findByUserOrderByNameAsc(user)
                .stream()
                .map(InventoryItemResponse::from)
                .toList();
    }

    public List<InventoryItemResponse> getForUserByType(User user, InventoryItemType type) {
        return inventoryItemRepository.findByUserAndTypeOrderByNameAsc(user, type)
                .stream()
                .map(InventoryItemResponse::from)
                .toList();
    }

    public List<InventoryItemResponse> getLowStock(User user) {
        return inventoryItemRepository.findByUserOrderByNameAsc(user)
                .stream()
                .filter(i -> i.getLowStockThreshold() != null && i.getQuantity() <= i.getLowStockThreshold())
                .map(InventoryItemResponse::from)
                .toList();
    }

    /** Adjusts stock by delta (positive = restock, negative = consumption). Rejects results below zero. */
    @Transactional
    public InventoryItemResponse adjustQuantity(User user, Long itemId, double delta) {
        InventoryItem item = getOwnedItem(user, itemId);
        double newQuantity = item.getQuantity() + delta;
        if (newQuantity < 0) {
            throw new BusinessRuleException(
                    "This adjustment would make the stock negative (" + item.getName() + " has "
                            + item.getQuantity() + " " + item.getUnit() + " remaining).");
        }
        item.setQuantity(newQuantity);
        return InventoryItemResponse.from(inventoryItemRepository.save(item));
    }

    @Transactional
    public void delete(User user, Long itemId) {
        InventoryItem item = getOwnedItem(user, itemId);
        inventoryItemRepository.delete(item);
    }

    private InventoryItem getOwnedItem(User user, Long itemId) {
        InventoryItem item = inventoryItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item", itemId));
        if (!item.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Inventory item", itemId);
        }
        return item;
    }
}
