package org.scitechdev.finalPorject.controller;

import org.scitechdev.finalPorject.Entity.Order;
import org.scitechdev.finalPorject.Entity.InventoryItem;
import org.scitechdev.finalPorject.service.OrderService;
import org.scitechdev.finalPorject.service.InventoryItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/buyer/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private InventoryItemService inventoryItemService;

    @GetMapping
    public String getOrders(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String buyerId = auth.getName();
        List<Order> orders = orderService.getOrdersByBuyerId(buyerId);
        for (Order order : orders) {
            List<String> itemNames = new ArrayList<>();
            List<String> quantities = new ArrayList<>();
            if (order.getProductIds() != null && order.getQuantities() != null) {
                for (int i = 0; i < order.getProductIds().size(); i++) {
                    String productId = order.getProductIds().get(i);
                    InventoryItem item = inventoryItemService.getItemById(productId);
                    itemNames.add(item != null ? item.getItemName() : "Unknown Product");
                    // Use the quantity from Firestore, default to "1" if missing
                    if (i < order.getQuantities().size()) {
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
            // Don't convert to List<Object> of String, just leave as is for display
        }
        model.addAttribute("orders", orders);
        return "buyer/orders";
    }

    @PostMapping("/place")
    public String placeOrder(@ModelAttribute Order order, @RequestParam(value = "quantities", required = false) List<Integer> quantities) {        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String buyerId = auth.getName();
        order.setBuyerId(buyerId);
        
        // Set order date if not already set
        if (order.getOrderDate() == null) {
            order.setOrderDate(LocalDateTime.now());
        }
        
        // If quantities are submitted as request param, use them
        if (quantities != null && !quantities.isEmpty()) {
            List<Object> intQuantities = new ArrayList<>(quantities);
            order.setQuantities(intQuantities);
        } else if (order.getQuantities() != null) {
            // Fallback: try to parse from model attribute
            List<Object> originalQuantities = order.getQuantities();
            List<Integer> intQuantities = new ArrayList<>();
            for (Object qtyObj : originalQuantities) {
                try {
                    int intQty = Integer.parseInt(qtyObj.toString());
                    intQuantities.add(intQty);
                } catch (Exception e) {
                    intQuantities.add(1); // fallback if conversion fails
                }
            }
            order.setQuantities(new ArrayList<Object>(intQuantities));
        }
        orderService.saveOrder(order);
        return "redirect:/buyer/orders";
    }

    @PostMapping("/delete")
    public String deleteOrder(@RequestParam String orderId) {
        orderService.deleteOrder(orderId);
        return "redirect:/buyer/orders";
    }
}
