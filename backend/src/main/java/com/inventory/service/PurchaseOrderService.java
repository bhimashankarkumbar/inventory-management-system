package com.inventory.service;

import com.inventory.entity.*;
import com.inventory.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Autowired
    public PurchaseOrderService(
            PurchaseOrderRepository purchaseOrderRepository,
            PurchaseOrderItemRepository purchaseOrderItemRepository,
            SupplierRepository supplierRepository,
            ProductRepository productRepository,
            UserRepository userRepository) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderItemRepository = purchaseOrderItemRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public PurchaseOrder createPurchaseOrder(Long supplierId, Long createdByUserId, List<LineItemRequest> items) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found"));

        User createdBy = userRepository.findById(createdByUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Purchase order must have at least one item");
        }

        PurchaseOrder order = new PurchaseOrder();
        order.setSupplier(supplier);
        order.setCreatedBy(createdBy);
        order.setStatus("OPEN");
        PurchaseOrder savedOrder = purchaseOrderRepository.save(order);

        for (LineItemRequest item : items) {
            Product product = productRepository.findById(item.productId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + item.productId()));

            PurchaseOrderItem poItem = new PurchaseOrderItem();
            poItem.setPurchaseOrder(savedOrder);
            poItem.setProduct(product);
            poItem.setQuantityOrdered(item.quantity());
            poItem.setUnitPrice(item.unitPrice());
            purchaseOrderItemRepository.save(poItem);
        }

        return savedOrder;
    }

    public List<PurchaseOrder> getAllPurchaseOrders() {
        return purchaseOrderRepository.findAll();
    }

    public PurchaseOrder getPurchaseOrderById(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Purchase order not found"));
    }

    public List<PurchaseOrderItem> getItemsForOrder(Long purchaseOrderId) {
        return purchaseOrderItemRepository.findByPurchaseOrderId(purchaseOrderId);
    }

    public record LineItemRequest(Long productId, Integer quantity, java.math.BigDecimal unitPrice) {}
}