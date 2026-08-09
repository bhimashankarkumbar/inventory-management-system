package com.inventory.controller;

import com.inventory.entity.InventoryTransaction;
import com.inventory.entity.User;
import com.inventory.repository.UserRepository;
import com.inventory.service.InventoryTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory-transactions")
public class InventoryTransactionController {

    private final InventoryTransactionService transactionService;
    private final UserRepository userRepository;

    @Autowired
    public InventoryTransactionController(InventoryTransactionService transactionService, UserRepository userRepository) {
        this.transactionService = transactionService;
        this.userRepository = userRepository;
    }

    private User getCurrentUser(Authentication authentication) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));
    }

    @PostMapping("/stock-in")
    public ResponseEntity<InventoryTransaction> stockIn(@RequestBody StockRequest request, Authentication authentication) {
        User user = getCurrentUser(authentication);
        InventoryTransaction txn = transactionService.recordStockIn(
                request.productId(), request.quantity(), user.getId(), request.referencePurchaseOrderId());
        return ResponseEntity.status(HttpStatus.CREATED).body(txn);
    }

    @PostMapping("/stock-out")
    public ResponseEntity<InventoryTransaction> stockOut(@RequestBody StockRequest request, Authentication authentication) {
        User user = getCurrentUser(authentication);
        InventoryTransaction txn = transactionService.recordStockOut(
                request.productId(), request.quantity(), user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(txn);
    }

    @PostMapping("/adjustment")
    public ResponseEntity<InventoryTransaction> requestAdjustment(@RequestBody AdjustmentRequest request, Authentication authentication) {
        User user = getCurrentUser(authentication);
        InventoryTransaction txn = transactionService.requestAdjustment(
                request.productId(), request.type(), request.quantity(), user.getId(), request.reason());
        return ResponseEntity.status(HttpStatus.CREATED).body(txn);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PatchMapping("/{id}/approve")
    public ResponseEntity<InventoryTransaction> approve(@PathVariable Long id, Authentication authentication) {
        User user = getCurrentUser(authentication);
        return ResponseEntity.ok(transactionService.approveAdjustment(id, user.getId()));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PatchMapping("/{id}/reject")
    public ResponseEntity<InventoryTransaction> reject(@PathVariable Long id, Authentication authentication) {
        User user = getCurrentUser(authentication);
        return ResponseEntity.ok(transactionService.rejectAdjustment(id, user.getId()));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<InventoryTransaction>> getHistory(@PathVariable Long productId) {
        return ResponseEntity.ok(transactionService.getHistoryForProduct(productId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/pending-approvals")
    public ResponseEntity<List<InventoryTransaction>> getPendingApprovals() {
        return ResponseEntity.ok(transactionService.getPendingApprovals());
    }

    public record StockRequest(Long productId, Integer quantity, Long referencePurchaseOrderId) {}
    public record AdjustmentRequest(Long productId, String type, Integer quantity, String reason) {}
}