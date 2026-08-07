package com.inventory.service;

import com.inventory.entity.Supplier;
import com.inventory.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;

    @Autowired
    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public Supplier createSupplier(Supplier supplier) {
        return supplierRepository.save(supplier);
    }

    public List<Supplier> getAllActiveSuppliers() {
        return supplierRepository.findByIsActiveTrue();
    }

    public Supplier getSupplierById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found"));
    }

    public Supplier updateSupplier(Long id, Supplier updatedData) {
        Supplier existing = getSupplierById(id);
        existing.setName(updatedData.getName());
        existing.setContactPerson(updatedData.getContactPerson());
        existing.setPhone(updatedData.getPhone());
        existing.setEmail(updatedData.getEmail());
        existing.setAddress(updatedData.getAddress());
        return supplierRepository.save(existing);
    }

    public void deactivateSupplier(Long id) {
        Supplier existing = getSupplierById(id);
        existing.setIsActive(false);
        supplierRepository.save(existing);
    }
}