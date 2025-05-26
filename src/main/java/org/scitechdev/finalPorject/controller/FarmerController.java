package org.scitechdev.finalPorject.controller;

import org.scitechdev.finalPorject.Entity.InventoryItem;
import org.scitechdev.finalPorject.service.FarmerOrderService;
import org.scitechdev.finalPorject.service.InventoryItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Controller
@RequestMapping("/farmer")
public class FarmerController {
    @Autowired
    private InventoryItemService inventoryItemService;

    @Autowired
    private FarmerOrderService farmerOrderService;    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String farmerId = auth.getName();
        var items = inventoryItemService.getItemsByFarmerId(farmerId);
        model.addAttribute("inventoryItems", items);
        model.addAttribute("totalInventory", items.size());
        model.addAttribute("userRole", "FARMER"); // Add user role for header conditional logic
        // Add buyer order requests for this farmer
        var orders = farmerOrderService.getOrdersByFarmerId(farmerId);
        // DEBUG: Log orders fetched for this farmer
        System.out.println("Farmer ID: " + farmerId);
        System.out.println("Orders fetched for farmer: " + orders.size());
        for (var order : orders) {
            System.out.println("Order ID: " + order.getOrderId() + ", Product IDs: " + order.getProductIds());
        }
        // Populate itemNames and quantities for each order
        for (var order : orders) {
            List<String> itemNames = new ArrayList<>();
            List<String> quantities = new ArrayList<>();
            if (order.getProductIds() != null) {
                for (int i = 0; i < order.getProductIds().size(); i++) {
                    String productId = order.getProductIds().get(i);
                    InventoryItem item = inventoryItemService.getItemById(productId);
                    itemNames.add(item != null ? item.getItemName() : "Unknown Product");
                    if (order.getQuantities() != null && i < order.getQuantities().size()) {
                        Object qtyObj = order.getQuantities().get(i);
                        String qtyStr;
                        if (qtyObj instanceof Number) {
                            qtyStr = String.valueOf(((Number) qtyObj).intValue());
                        } else {
                            try {
                                qtyStr = String.valueOf(Double.valueOf(qtyObj.toString()).intValue());
                            } catch (Exception e) {
                                qtyStr = qtyObj.toString();
                            }
                        }
                        quantities.add(qtyStr);
                    } else {
                        quantities.add("1");
                    }
                }
            }
            order.setItemNames(itemNames);
            // Don't set order.setQuantities(quantities) since setter expects List<Object> and we only need to display quantities as strings in the view
        }
        model.addAttribute("orderRequests", orders);
        model.addAttribute("pendingOrders", orders.size());
        return "Farmer_Dashboard";
    }

    @PostMapping("/add-crop")
    public String addCrop(@ModelAttribute("newItem") InventoryItem item,
                         @RequestParam(value = "cropImage", required = false) MultipartFile cropImage,
                         Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String farmerId = auth.getName();
            item.setFarmerId(farmerId);
            // Location is set by form binding: item.setLocation(item.getLocation());
            // Handle image upload if a file is provided
            if (cropImage != null && !cropImage.isEmpty()) {
                // Save the file to a local folder (e.g., src/main/resources/static/img/)
                String uploadDir = "src/main/resources/static/img/";
                File uploadFolder = new File(uploadDir);
                if (!uploadFolder.exists()) uploadFolder.mkdirs();
                String fileName = System.currentTimeMillis() + "_" + cropImage.getOriginalFilename();
                File dest = new File(uploadDir + fileName);
                cropImage.transferTo(dest);
                // Set the image path relative to static for web access
                item.setItemImage("/img/" + fileName);
            }
            inventoryItemService.saveItem(item);
        } catch (IOException e) {
            model.addAttribute("error", "Failed to upload image: " + e.getMessage());
            model.addAttribute("inventoryItems", inventoryItemService.getAllItems());
            return "Farmer_Dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to add crop: " + e.getMessage());
            model.addAttribute("inventoryItems", inventoryItemService.getAllItems());
            return "Farmer_Dashboard";
        }
        return "redirect:/farmer/dashboard";
    }    @PostMapping("/delete-crop")
    public String deleteCrop(@RequestParam("itemId") String itemId) {
        inventoryItemService.deleteItem(itemId);
        return "redirect:/farmer/dashboard";
    }

    @GetMapping("/edit-crop/{itemId}")
    public String editCrop(@PathVariable String itemId, Model model) {
        InventoryItem item = inventoryItemService.getItemById(itemId);
        model.addAttribute("editItem", item);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String farmerId = auth.getName();
        var items = inventoryItemService.getItemsByFarmerId(farmerId);
        model.addAttribute("inventoryItems", items);
        return "inventory";
    }

    @PostMapping("/update-crop")
    public String updateCrop(@ModelAttribute("editItem") InventoryItem item, Model model) {
        System.out.println("[DEBUG] update-crop called for itemId: " + item.getItemId());
        System.out.println("[DEBUG] Name: " + item.getItemName() + ", Price: " + item.getItemPrice() + ", Quantity: " + item.getItemQuantity());
        inventoryItemService.saveItem(item);
        // Set the editItem in the model so the modal will open after redirect
        model.addAttribute("editItem", item);
        return "redirect:/farmer/inventory?editItemId=" + item.getItemId();
    }

    @PostMapping("/order/accept")
    public String acceptOrder(@RequestParam("orderId") String orderId) {
        // When farmer accepts, set status to 'Processing' for buyer's view
        farmerOrderService.updateOrderStatus(orderId, "Processing");
        return "redirect:/farmer/dashboard";
    }

    @PostMapping("/order/reject")
    public String rejectOrder(@RequestParam("orderId") String orderId) {
        farmerOrderService.updateOrderStatus(orderId, "Rejected");
        return "redirect:/farmer/dashboard";
    }    @GetMapping("/inventory")
    public String inventoryPage(@RequestParam(value = "editItemId", required = false) String editItemId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String farmerId = auth.getName();
        var items = inventoryItemService.getItemsByFarmerId(farmerId);
        model.addAttribute("inventoryItems", items);
        model.addAttribute("userRole", "FARMER"); // Add user role for header conditional logic
        // If editItemId is present, fetch and set editItem
        if (editItemId != null && !editItemId.isBlank()) {
            InventoryItem editItem = inventoryItemService.getItemById(editItemId);
            model.addAttribute("editItem", editItem);
        } else if (!model.containsAttribute("editItem")) {
            model.addAttribute("editItem", new InventoryItem());
        }
        model.addAttribute("newItem", new InventoryItem());
        return "/inventory";
    }

    @ModelAttribute("newItem")
    public InventoryItem newItem() {
        return new InventoryItem();
    }
}
