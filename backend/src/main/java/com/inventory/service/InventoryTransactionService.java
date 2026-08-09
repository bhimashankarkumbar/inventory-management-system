package com.inventory.service;

import com.inventory.entity.*;
import com.inventory.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventoryTransactionService {

    private final InventoryTransactionRepository transactionRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Autowired
    public InventoryTransactionService(
            InventoryTransactionRepository transactionRepository,
            ProductRepository productRepository,
            UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public InventoryTransaction recordStockIn(Long productId, Integer quantity, Long userId, Long referencePurchaseOrderId) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        User performedBy = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        int before = product.getCurrentQuantity();
        int after = before + quantity;

        InventoryTransaction txn = new InventoryTransaction();
        txn.setProduct(product);
        txn.setType("STOCK_IN");
        txn.setQuantity(quantity);
        txn.setQuantityBefore(before);
        txn.setQuantityAfter(after);
        txn.setStatus("COMPLETED");
        txn.setPerformedBy(performedBy);

        product.setCurrentQuantity(after);

        try {
            productRepository.save(product);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new IllegalStateException("Product was updated by another transaction. Please refresh and try again.");
        }

        return transactionRepository.save(txn);
    }

    @Transactional
    public InventoryTransaction recordStockOut(Long productId, Integer quantity, Long userId) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        User performedBy = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        int before = product.getCurrentQuantity();
        if (before < quantity) {
            throw new IllegalArgumentException("Insufficient stock available. Please refresh the inventory and try again.");
        }
        int after = before - quantity;

        InventoryTransaction txn = new InventoryTransaction();
        txn.setProduct(product);
        txn.setType("STOCK_OUT");
        txn.setQuantity(quantity);
        txn.setQuantityBefore(before);
        txn.setQuantityAfter(after);
        txn.setStatus("COMPLETED");
        txn.setPerformedBy(performedBy);

        product.setCurrentQuantity(after);

        try {
            productRepository.save(product);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new IllegalStateException("Insufficient stock available. Please refresh the inventory and try again.");
        }

        return transactionRepository.save(txn);
    }

    @Transactional
    public InventoryTransaction requestAdjustment(Long productId, String type, Integer quantity, Long userId, String reason) {
        if (!List.of("DAMAGED", "LOST", "ADJUSTMENT", "CORRECTION").contains(type)) {
            throw new IllegalArgumentException("Invalid adjustment type");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("A reason is required for this transaction type");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        User performedBy = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        InventoryTransaction txn = new InventoryTransaction();
        txn.setProduct(product);
        txn.setType(type);
        txn.setQuantity(quantity);
        txn.setQuantityBefore(product.getCurrentQuantity());
        txn.setQuantityAfter(product.getCurrentQuantity() - quantity);
        txn.setStatus("PENDING_APPROVAL");
        txn.setPerformedBy(performedBy);
        txn.setReason(reason);

        return transactionRepository.save(txn);
    }

    @Transactional
    public InventoryTransaction approveAdjustment(Long transactionId, Long approverId) {
        InventoryTransaction txn = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        if (!"PENDING_APPROVAL".equals(txn.getStatus())) {
            throw new IllegalArgumentException("Only pending transactions can be approved");
        }

        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new IllegalArgumentException("Approver not found"));

        Product product = txn.getProduct();
        int currentQty = product.getCurrentQuantity();
        int newQty = currentQty - txn.getQuantity();

        if (newQty < 0) {
            throw new IllegalArgumentException("Cannot approve: resulting stock would be negative");
        }

        txn.setQuantityBefore(currentQty);
        txn.setQuantityAfter(newQty);
        txn.setStatus("COMPLETED");
        txn.setApprovedBy(approver);

        product.setCurrentQuantity(newQty);

        try {
            productRepository.save(product);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new IllegalStateException("Product was updated by another transaction. Please refresh and try again.");
        }

        return transactionRepository.save(txn);
    }

    @Transactional
    public InventoryTransaction rejectAdjustment(Long transactionId, Long approverId) {
        InventoryTransaction txn = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        if (!"PENDING_APPROVAL".equals(txn.getStatus())) {
            throw new IllegalArgumentException("Only pending transactions can be rejected");
        }

        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new IllegalArgumentException("Approver not found"));

        txn.setStatus("REJECTED");
        txn.setApprovedBy(approver);

        return transactionRepository.save(txn);
    }

    public List<InventoryTransaction> getHistoryForProduct(Long productId) {
        return transactionRepository.findByProductIdOrderByCreatedAtDesc(productId);
    }

    public List<InventoryTransaction> getPendingApprovals() {
        return transactionRepository.findByStatus("PENDING_APPROVAL");
    }
}