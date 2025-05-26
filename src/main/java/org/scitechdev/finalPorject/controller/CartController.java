package org.scitechdev.finalPorject.controller;

import org.scitechdev.finalPorject.Entity.CartItem;
import org.scitechdev.finalPorject.service.CartItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/buyer/cart")
public class CartController {
    @Autowired
    private CartItemService cartItemService;

    // Helper method to format price as Philippine Peso
    public static String formatPeso(double amount) {
        return String.format("₱%,.2f", amount);
    }

}
