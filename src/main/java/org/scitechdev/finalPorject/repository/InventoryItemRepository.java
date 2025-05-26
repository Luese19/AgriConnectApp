package org.scitechdev.finalPorject.repository;

import java.util.List;

import org.scitechdev.finalPorject.Entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, String> {

    List<InventoryItem> findAll();

    InventoryItem save(InventoryItem item);
}
