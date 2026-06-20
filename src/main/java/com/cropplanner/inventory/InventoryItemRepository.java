package com.cropplanner.inventory;

import com.cropplanner.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    List<InventoryItem> findByUserOrderByNameAsc(User user);
    List<InventoryItem> findByUserAndTypeOrderByNameAsc(User user, InventoryItemType type);
}
