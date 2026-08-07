package com.inventory.controller;

import com.inventory.entity.PurchaseOrder;
import com.inventory.entity.PurchaseOrderItem;
import com.inventory.entity.User;
import com.inventory.service.PurchaseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;
    private final com.inventory.repository.UserRepository userRepository;

    @Autowired
    public PurchaseOrderController(PurchaseOrderService purchaseOrderService, com.inventory.repository.UserRepository userRepository) {
        this.purchaseOrderService = purchaseOrderService;
        this.userRepository = userRepository;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping
    public ResponseEntity<PurchaseOrder> createPurchaseOrder(
            @RequestBody CreatePurchaseOrderRequest request,
            Authentication authentication) {

        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));

        PurchaseOrder created = purchaseOrderService.createPurchaseOrder(
                request.supplierId(), currentUser.getId(), request.items());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<PurchaseOrder>> getAllPurchaseOrders() {
        return ResponseEntity.ok(purchaseOrderService.getAllPurchaseOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrder> getPurchaseOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.getPurchaseOrderById(id));
    }

    @GetMapping("/{id}/items")
    public ResponseEntity<List<PurchaseOrderItem>> getItemsForOrder(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.getItemsForOrder(id));
    }

    public record CreatePurchaseOrderRequest(
            Long supplierId,
            List<PurchaseOrderService.LineItemRequest> items) {}
}