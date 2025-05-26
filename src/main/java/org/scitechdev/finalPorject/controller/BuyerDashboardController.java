package org.scitechdev.finalPorject.controller;

import java.time.LocalDateTime;
import java.util.Collections;
import org.scitechdev.finalPorject.service.InventoryItemService;
import org.scitechdev.finalPorject.service.CartItemService;
import org.scitechdev.finalPorject.service.OrderService;
import org.scitechdev.finalPorject.Entity.CartItem;
import org.scitechdev.finalPorject.Entity.Order;
import org.scitechdev.finalPorject.Entity.InventoryItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/buyer")
public class BuyerDashboardController {
    @Autowired
    private InventoryItemService inventoryItemService;
    @Autowired
    private CartItemService cartItemService;
    @Autowired
    private OrderService orderService;

    @GetMapping({"", "/"})
    public String buyerHome(Model model) {
        // Add any model attributes needed for the buyer dashboard
        System.out.println("Buyer dashboard controller accessed");

        // Check if user is authenticated
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("User authenticated as: " + auth.getName() + " with authorities: " + auth.getAuthorities());

        model.addAttribute("pageTitle", "Buyer Dashboard");
        // Fetch all products and add to model
        model.addAttribute("products", inventoryItemService.getAllItems());
        List<CartItem> cartItems = cartItemService.getCartItemsByBuyerId(auth.getName());
        int cartCount = cartItems.stream().mapToInt(CartItem::getQuantity).sum();
        model.addAttribute("cartCount", cartCount);
        return "buyer/dashboard";
    }    @GetMapping("/products")
    public String productListing(
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "priceRange", required = false) String priceRange,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "search", required = false) String search,
            Model model) {
        
        model.addAttribute("pageTitle", "Product Listing");
        
        // Get all products
        List<InventoryItem> allProducts = inventoryItemService.getAllItems();
        List<InventoryItem> filteredProducts = new ArrayList<>(allProducts);
        
        // Apply filters
        if (search != null && !search.trim().isEmpty()) {
            filteredProducts = filteredProducts.stream()
                .filter(p -> p.getItemName().toLowerCase().contains(search.toLowerCase()) ||
                           p.getItemDescription().toLowerCase().contains(search.toLowerCase()))
                .collect(Collectors.toList());
        }
        
        if (category != null && !category.isEmpty()) {
            filteredProducts = filteredProducts.stream()
                .filter(p -> category.equalsIgnoreCase(p.getItemCategory()))
                .collect(Collectors.toList());
        }
        
        if (priceRange != null && !priceRange.isEmpty()) {
            filteredProducts = filteredProducts.stream()
                .filter(p -> {
                    try {
                        double price = Double.parseDouble(p.getItemPrice());
                        switch (priceRange) {
                            case "under5": return price < 5;
                            case "5-10": return price >= 5 && price <= 10;
                            case "10-20": return price >= 10 && price <= 20;
                            case "over20": return price > 20;
                            default: return true;
                        }
                    } catch (NumberFormatException e) {
                        return true;
                    }
                })
                .collect(Collectors.toList());
        }
        
        if (location != null && !location.isEmpty()) {
            // Simple location filtering - in real app, this would use geolocation
            filteredProducts = filteredProducts.stream()
                .filter(p -> p.getLocation() != null && 
                           p.getLocation().toLowerCase().contains(location.toLowerCase()))
                .collect(Collectors.toList());
        }
        
        // Apply sorting
        if (sortBy != null && !sortBy.isEmpty()) {
            switch (sortBy) {
                case "priceAsc":
                    filteredProducts.sort((a, b) -> {
                        try {
                            return Double.compare(Double.parseDouble(a.getItemPrice()), 
                                                Double.parseDouble(b.getItemPrice()));
                        } catch (NumberFormatException e) {
                            return 0;
                        }
                    });
                    break;
                case "priceDesc":
                    filteredProducts.sort((a, b) -> {
                        try {
                            return Double.compare(Double.parseDouble(b.getItemPrice()), 
                                                Double.parseDouble(a.getItemPrice()));
                        } catch (NumberFormatException e) {
                            return 0;
                        }
                    });
                    break;
                case "nameAsc":
                    filteredProducts.sort((a, b) -> a.getItemName().compareToIgnoreCase(b.getItemName()));
                    break;
                case "nameDesc":
                    filteredProducts.sort((a, b) -> b.getItemName().compareToIgnoreCase(a.getItemName()));
                    break;
                default:
                    // Keep original order for "recommended"
                    break;
            }
        }
        
        model.addAttribute("products", filteredProducts);
        model.addAttribute("currentCategory", category);
        model.addAttribute("currentPriceRange", priceRange);
        model.addAttribute("currentLocation", location);
        model.addAttribute("currentSortBy", sortBy);
        model.addAttribute("currentSearch", search);
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<CartItem> cartItems = cartItemService.getCartItemsByBuyerId(auth.getName());
        int cartCount = cartItems.stream().mapToInt(CartItem::getQuantity).sum();
        model.addAttribute("cartCount", cartCount);
        
        return "buyer/products";
    }

    @GetMapping("/cart")
    public String cart(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String buyerId = auth.getName();
        List<CartItem> cartItems = cartItemService.getCartItemsByBuyerId(buyerId);
        int cartCount = cartItems.stream().mapToInt(CartItem::getQuantity).sum();
        List<Map<String, Object>> cartDetails = new ArrayList<>();
        double cartTotal = 0.0;
        for (CartItem cartItem : cartItems) {
            InventoryItem product = inventoryItemService.getItemById(cartItem.getProductId());
            if (product != null) {
                double price = 0.0;
                try { price = Double.parseDouble(product.getItemPrice()); } catch (Exception e) { }
                double total = price * cartItem.getQuantity();
                cartTotal += total;
                Map<String, Object> detail = new HashMap<>();
                detail.put("cartItem", cartItem);
                detail.put("product", product);
                detail.put("price", price);
                detail.put("total", total);
                cartDetails.add(detail);
            }
        }
        model.addAttribute("pageTitle", "Shopping Cart");
        model.addAttribute("cartDetails", cartDetails);
        model.addAttribute("cartTotal", cartTotal);
        model.addAttribute("cartCount", cartCount);
        return "buyer/cart";
    }

    

    @GetMapping("/chat")
    public String chatSupport(Model model) {
        model.addAttribute("pageTitle", "Chat with Farmers");
        return "buyer/chat";
    }    @PostMapping("/cart/add")
    public String addToCart(@RequestParam("productId") String productId, 
                           @RequestParam(value = "quantity", defaultValue = "1") int quantity,
                           RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String buyerId = auth.getName();
        
        // Check if item already exists in cart
        List<CartItem> existingItems = cartItemService.getCartItemsByBuyerId(buyerId);
        CartItem existingItem = existingItems.stream()
            .filter(item -> item.getProductId().equals(productId))
            .findFirst()
            .orElse(null);
            
        if (existingItem != null) {
            // Update quantity if item already exists
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            cartItemService.saveCartItem(existingItem);
            redirectAttributes.addFlashAttribute("message", "Product quantity updated in cart!");
        } else {
            // Add new item to cart
            CartItem cartItem = new CartItem();
            cartItem.setBuyerId(buyerId);
            cartItem.setProductId(productId);
            cartItem.setQuantity(quantity);
            cartItemService.saveCartItem(cartItem);
            redirectAttributes.addFlashAttribute("message", "Product added to cart!");
        }
        
        return "redirect:/buyer/products";
    }

    @PostMapping("/order")
    public String orderProduct(@RequestParam("productId") String productId, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String buyerId = auth.getName();
        // Remove the item from cart if it exists
        List<CartItem> cartItems = cartItemService.getCartItemsByBuyerId(buyerId);
        cartItems.stream()
            .filter(item -> item.getProductId().equals(productId))
            .findFirst()
            .ifPresent(item -> cartItemService.deleteCartItem(item.getCartItemId()));
        // Get product price for totalAmount
        InventoryItem product = inventoryItemService.getItemById(productId);
        double totalAmount = 0.0;        if (product != null) {
            try { totalAmount = Double.parseDouble(product.getItemPrice()); } catch (Exception e) { }
        }
        Order order = new Order();
        order.setBuyerId(buyerId);
        order.setProductIds(Collections.singletonList(productId));
        order.setTotalAmount(totalAmount);
        order.setStatus("Placed");
        order.setOrderDate(LocalDateTime.now());
        orderService.saveOrder(order);
        redirectAttributes.addFlashAttribute("message", "Order placed successfully!");
        return "redirect:/buyer/products";
    }

    @PostMapping("/cart/update")
    public String updateCartItem(@RequestParam("cartItemId") String cartItemId, @RequestParam("quantity") int quantity, RedirectAttributes redirectAttributes) {
        CartItem cartItem = cartItemService.getCartItemsByBuyerId(SecurityContextHolder.getContext().getAuthentication().getName())
            .stream().filter(item -> item.getCartItemId().equals(cartItemId)).findFirst().orElse(null);
        if (cartItem != null && quantity > 0) {
            cartItem.setQuantity(quantity);
            cartItemService.saveCartItem(cartItem);
            redirectAttributes.addFlashAttribute("message", "Cart updated.");
        }
        return "redirect:/buyer/cart";
    }

    @PostMapping("/cart/delete")
    public String deleteCartItem(@RequestParam("cartItemId") String cartItemId, RedirectAttributes redirectAttributes) {
        cartItemService.deleteCartItem(cartItemId);
        redirectAttributes.addFlashAttribute("message", "Item removed from cart.");
        return "redirect:/buyer/cart";
    }

    @PostMapping("/order/all")
    public String orderAllCartItems(
            @RequestParam(value = "deliveryOption", required = false) String deliveryOption,
            @RequestParam(value = "deliveryAddress", required = false) String deliveryAddress,
            @RequestParam(value = "paymentMethod", required = false) String paymentMethod,
            RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String buyerId = auth.getName();
        List<CartItem> cartItems = cartItemService.getCartItemsByBuyerId(buyerId);
        if (cartItems.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Your cart is empty.");
            return "redirect:/buyer/cart";
        }
        double totalAmount = 0.0;
        List<String> productIds = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            InventoryItem product = inventoryItemService.getItemById(cartItem.getProductId());
            if (product != null) {
                try { totalAmount += Double.parseDouble(product.getItemPrice()) * cartItem.getQuantity(); } catch (Exception e) { }
                productIds.add(cartItem.getProductId());
            }
        }        Order order = new Order();
        order.setBuyerId(buyerId);
        order.setProductIds(productIds);
        order.setTotalAmount(totalAmount);
        order.setStatus("Placed");
        order.setOrderDate(LocalDateTime.now());
        // Save delivery and payment info if your Order entity supports it
        if (deliveryOption != null) order.setDeliveryOption(deliveryOption);
        if (deliveryAddress != null && !deliveryAddress.isBlank()) order.setDeliveryAddress(deliveryAddress);
        if (paymentMethod != null) order.setPaymentMethod(paymentMethod);
        orderService.saveOrder(order);
        // Clear the cart
        for (CartItem cartItem : cartItems) {
            cartItemService.deleteCartItem(cartItem.getCartItemId());
        }
        redirectAttributes.addFlashAttribute("message", "Order placed for all cart items!");
        return "redirect:/buyer/orders";
    }

    @GetMapping("/checkout")
    public String checkoutPage(@RequestParam(value = "productId", required = false) String productId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String buyerId = auth.getName();
        if (productId != null && !productId.isBlank()) {
            InventoryItem product = inventoryItemService.getItemById(productId);
            if (product == null) {
                model.addAttribute("errorMessage", "Product not found.");
                model.addAttribute("product", null);
                model.addAttribute("cartDetails", new ArrayList<>());
                model.addAttribute("subtotal", 0.0);
                model.addAttribute("shipping", 0.0);
                model.addAttribute("total", 0.0);
                return "buyer/checkout";
            }
            model.addAttribute("product", product); // Always add, even if null
            model.addAttribute("cartDetails", new ArrayList<>()); // Always add
            double subtotal = 0.0;
            try { subtotal = Double.parseDouble(product.getItemPrice()); } catch (Exception e) { }
            double shipping = subtotal > 0 ? 5.0 : 0.0;
            double total = subtotal + shipping;
            model.addAttribute("subtotal", subtotal);
            model.addAttribute("shipping", shipping);
            model.addAttribute("total", total);
        } else {
            // Cart-based checkout (existing logic)
            List<CartItem> cartItems = cartItemService.getCartItemsByBuyerId(buyerId);
            List<Map<String, Object>> cartDetails = new ArrayList<>();
            double subtotal = 0.0;
            double shipping = 0.0;
            for (CartItem cartItem : cartItems) {
                InventoryItem product = inventoryItemService.getItemById(cartItem.getProductId());
                if (product != null) {
                    double price = 0.0;
                    try { price = Double.parseDouble(product.getItemPrice()); } catch (Exception e) { }
                    double itemTotal = price * cartItem.getQuantity();
                    subtotal += itemTotal;
                    Map<String, Object> detail = new HashMap<>();
                    detail.put("cartItem", cartItem);
                    detail.put("product", product);
                    detail.put("price", price);
                    detail.put("total", itemTotal);
                    cartDetails.add(detail);
                }
            }
            if (subtotal > 0) shipping = 5.0;
            double total = subtotal + shipping;
            model.addAttribute("product", null); // Always add
            model.addAttribute("cartDetails", cartDetails);
            model.addAttribute("subtotal", subtotal);
            model.addAttribute("shipping", shipping);
            model.addAttribute("total", total);
        }
        return "buyer/checkout";
    }

    @PostMapping("/checkout/submit")
    public String submitCheckout(
            @RequestParam(value = "productId", required = false) String productId,
            @RequestParam(value = "quantity", required = false) Integer quantity,
            @RequestParam(value = "cartItemIds", required = false) List<String> cartItemIds,
            @RequestParam(value = "quantities", required = false) List<Integer> quantities,
            @RequestParam("deliveryOption") String deliveryOption,
            @RequestParam(value = "deliveryAddress", required = false) String deliveryAddress,
            @RequestParam("paymentMethod") String paymentMethod,
            RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String buyerId = auth.getName();
        double totalAmount = 0.0;
        List<String> productIds = new ArrayList<>();
        if (productId != null && quantity != null) {
            // Single product direct order
            InventoryItem product = inventoryItemService.getItemById(productId);
            if (product != null) {
                try { totalAmount = Double.parseDouble(product.getItemPrice()) * quantity; } catch (Exception e) { }
                productIds.add(productId);
            }
        } else if (cartItemIds != null && quantities != null) {
            // Cart-based order
            for (int idx = 0; idx < cartItemIds.size(); idx++) {
                final int i = idx;
                CartItem cartItem = cartItemService.getCartItemsByBuyerId(buyerId)
                    .stream().filter(item -> item.getCartItemId().equals(cartItemIds.get(i))).findFirst().orElse(null);
                if (cartItem != null) {
                    cartItem.setQuantity(quantities.get(i));
                    cartItemService.saveCartItem(cartItem);
                    InventoryItem product = inventoryItemService.getItemById(cartItem.getProductId());
                    if (product != null) {
                        try { totalAmount += Double.parseDouble(product.getItemPrice()) * cartItem.getQuantity(); } catch (Exception e) { }
                        productIds.add(cartItem.getProductId());
                    }
                }
            }
            // Clear the cart
            for (String cartItemId : cartItemIds) {
                cartItemService.deleteCartItem(cartItemId);
            }
        }        Order order = new Order();
        order.setBuyerId(buyerId);
        order.setProductIds(productIds);
        order.setTotalAmount(totalAmount);
        order.setStatus("Placed");
        order.setOrderDate(LocalDateTime.now());
        order.setDeliveryOption(deliveryOption);
        order.setPaymentMethod(paymentMethod);
        if (deliveryAddress != null && !deliveryAddress.isBlank()) {
            order.setDeliveryAddress(deliveryAddress);
        }
        // Store quantities in the order (for both single and cart-based)
        if (productId != null && quantity != null) {
            List<Object> qList = new ArrayList<>();
            qList.add(quantity);
            order.setQuantities(qList);
        } else if (quantities != null && !quantities.isEmpty()) {
            List<Object> qList = new ArrayList<>(quantities);
            order.setQuantities(qList);
        }
        orderService.saveOrder(order);
        redirectAttributes.addFlashAttribute("message", "Order placed successfully!");
        return "redirect:/buyer/orders";
    }
}
