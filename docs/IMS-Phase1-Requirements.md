# Inventory Management System — Phase 1: Business & Software Requirements

## 1. Overview

A single-warehouse inventory management system for internal company use, tracking
the full lifecycle of a product from supplier purchase through to stock dispatch
or adjustment — with an immutable audit trail of every inventory change and a
separate log of user activity for security auditing.

**Users (V1):** Admin, Warehouse Manager, Warehouse Staff (all internal employees).
**Out of scope for V1:** Supplier portal, customer-facing features, automatic
purchase order generation, multi-warehouse support.

---

## 2. Functional Requirements

### 2.1 User & Access Management
- FR-1: System supports three internal roles: Admin, Warehouse Manager, Warehouse Staff.
- FR-2: Admin can create, update, deactivate user accounts and assign roles.

### 2.2 Category Management
- FR-3: Admin/Manager can create, update, deactivate, and view product categories.
- FR-4: A deactivated category is hidden from new product assignment but remains valid for existing products (their history/reports must not break).

### 2.3 Product Management
- FR-5: Admin/Manager can create, update, and deactivate products (SKU, barcode, name, category, unit, min-stock threshold).
- FR-6: Every product belongs to exactly **one** category (foreign key relationship, not a free-text field).
- FR-7: Every product has both a **SKU** and a **barcode** field. Barcode is stored for future scanning support but is not scanned/validated in V1 — it's entered/edited like any other text field.
- FR-8: Products can be searched/filtered by SKU, barcode, name, category.
- FR-9: Product list is paginated.
- FR-10: Products are **never hard-deleted**. Deactivating a product (marking it Inactive) hides it from new transactions while preserving it — and all its historical transactions — in the system.

### 2.4 Supplier Management
- FR-11: Admin/Manager can create and maintain supplier records (no supplier login in V1).
- FR-12: Suppliers are **never hard-deleted** — deactivated instead, so past purchase orders remain valid and reportable.

### 2.5 Purchase Orders (Simplified)
- FR-13: Manager can create a purchase order against a supplier for one or more products.
- FR-14: Receiving a purchase order generates a Stock In transaction for the full ordered quantity.
- FR-15 *(V1 scope note)*: No partial receiving, no PO approval workflow, no invoice management — a PO is either open or received, in full.

### 2.6 Stock Transactions
- FR-16: Warehouse Staff can record **Stock In** (receiving goods).
- FR-17: Warehouse Staff can record **Stock Out** (dispatch/sale).
- FR-18: Normal Stock In/Out transactions are auto-processed — no approval required.
- FR-19: The following transaction types require Warehouse Manager approval before they affect stock levels:
  - Damaged product write-off
  - Lost product write-off
  - Manual stock adjustment
  - Corrections to previous transactions
- FR-20: **No transaction is ever edited or deleted.** Corrections are made only via a new, linked Stock Adjustment transaction referencing the original.

### 2.7 Stock Level & Alerts
- FR-21: Each product has a minimum stock threshold.
- FR-22: When stock falls below threshold, product is flagged "Low Stock."
- FR-23: Low-stock alerts are shown **on the in-app dashboard only** in V1 — no email/SMS/push.

### 2.8 Concurrency Rule
- FR-24: If two simultaneous Stock Out requests would drop inventory below zero, only the first to commit succeeds. The second fails with a clear "insufficient stock, please refresh" message. Inventory quantity must never go negative.

### 2.9 Reports (Simplified)
- FR-25: Current stock levels report.
- FR-26: Low stock report (all products under threshold).
- FR-27: Stock movement history within a date range.

### 2.10 Dashboard
- FR-28: Dashboard displays:
  - Total number of products
  - Current total inventory count
  - Number of low-stock items
  - Count of today's stock transactions (In + Out)

### 2.11 User Activity Log (Auditing)
- FR-29: The system records a log of user actions, separate from the inventory transaction log, including at minimum:
  - User login (success/failure)
  - Product creation/update/deactivation
  - Category creation/update/deactivation
  - Supplier creation/update/deactivation
  - Purchase order creation
- FR-30: Each activity log entry records: which user, what action, on which entity/record, and when.
- FR-31: Activity logs are **append-only** — never edited or deleted, same principle as inventory transactions, since an editable audit log defeats its own purpose.

> **Why two separate logs (Inventory Transaction Log vs. User Activity Log)?**
> They answer different questions. The inventory log answers *"what happened to this stock?"* (business data — quantities, before/after states). The activity log answers *"who did what, and when, in this system?"* (security/accountability data — logins, record changes, regardless of whether they touched stock quantity at all, e.g. a category rename). Keeping them separate means each can be modeled and queried simply, instead of one bloated "everything" table trying to serve two different purposes.

### 2.12 Role Permission Matrix

| Action | Admin | Manager | Staff |
|---|---|---|---|
| Manage users/system settings | Yes | No | No |
| Manage categories, products & suppliers | Yes | Yes | View only |
| Create purchase orders | Yes | Yes | No |
| Record Stock In / Stock Out | Yes | Yes | Yes |
| Approve exceptional adjustments | Yes | Yes | No |
| View reports & dashboard | Yes | Yes | Limited |
| View user activity log | Yes | No | No |

---

## 3. Non-Functional Requirements

| Category | Requirement |
|---|---|
| **Data integrity** | Stock quantity must never be negative or inconsistent under concurrent writes (optimistic or pessimistic locking at the DB layer). |
| **Auditability** | Every inventory-affecting transaction is immutable and logged with actor, timestamp, before/after quantity. User-level actions (logins, record changes) are separately logged for security auditing. |
| **Data retention** | Products and suppliers are soft-deleted (Active/Inactive flag), never hard-deleted, so historical transactions and reports always resolve correctly. |
| **Security** | Passwords hashed (BCrypt); JWT-based stateless authentication; role-based authorization enforced server-side, not just in UI. |
| **Performance** | The application should support efficient searching, filtering, pagination, and reporting for approximately 5,000-10,000 products using proper database indexing. |
| **Usability** | Clear, actionable error messages (e.g., insufficient stock) rather than raw exceptions. |
| **Maintainability** | Clean layered architecture (controller/service/repository), so features can be added without cross-cutting rewrites. |
| **Availability** | Single-instance deployment is acceptable for V1 (no HA requirement) - this is a portfolio project, not a live production system. |

---

## 4. Explicit Out-of-Scope (V1) - Planned for Version 2

- Multi-warehouse support and inter-warehouse transfers
- Partial PO receiving, PO approval workflow, invoice management
- Purchase history by supplier (report)
- Email / SMS / push notifications for low stock
- Barcode scanning (field exists in schema now; scanning UI/logic is V2)
- Supplier or customer login/portal
- Automatic purchase order generation on low stock

---

## 5. Open Design Questions (carry into Phase 3 - DB Design)

- Should barcode have a uniqueness constraint in V1, or only enforced when scanning is added in V2?
- Should the inventory transaction log be one unified table with a `type` discriminator, or separate tables per transaction type?
- Should the User Activity Log store old/new values for updates (e.g., "price changed from X to Y"), or just record that an update happened? (Affects table width and whether you need a generic JSON/diff column.)
