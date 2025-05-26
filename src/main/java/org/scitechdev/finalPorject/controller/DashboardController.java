package org.scitechdev.finalPorject.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.scitechdev.finalPorject.service.InventoryService;

@Controller
public class DashboardController {
    
    @Autowired
    private InventoryService inventoryService;


      @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("newItem", new org.scitechdev.finalPorject.Entity.InventoryItem());
        model.addAttribute("inventoryItems", new java.util.ArrayList<>()); // or fetch actual items if possible
        return "Farmer_Dashboard";
    }

    
}
