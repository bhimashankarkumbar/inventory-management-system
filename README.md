# Inventory Management System

A full-stack inventory management system built with **Spring Boot**, **Spring Security (JWT)**, **MySQL**, and **React** — designed and built from requirements through deployment as a demonstration of backend architecture, secure API design, and full-stack integration.

> This is a V1, deliberately scoped-down system. See [Design Decisions](#design-decisions) and [Roadmap](#roadmap--v2) for what was intentionally left out and why.

## Screenshots

![Login](docs/image.png)

![Products](docs/image-1.png)

---

## Table of Contents
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Key Design Decisions](#design-decisions)
- [Getting Started](#getting-started)
- [API Overview](#api-overview)
- [Security](#security)
- [Roadmap / V2](#roadmap--v2)

---

## Features

- **JWT-based authentication** with role-based access control (Admin / Manager / Staff)
- **Category & Product management** with soft delete, low-stock highlighting, and category relationships
- **Supplier management**
- **Purchase Orders** with multi-line-item creation in a single request
- **Inventory transactions** (Stock In / Stock Out) with:
  - Optimistic locking to prevent race conditions on concurrent stock updates
  - Full transaction history per product
  - An approval workflow for exceptional transactions (damaged/lost/adjustment/correction) — normal Stock In/Out auto-completes, sensitive changes require Manager approval
- **User activity log** — a separate, append-only audit trail of user actions (logins, record changes), distinct from the inventory transaction log
- A working React frontend: login, protected routing, and functional CRUD screens wired to the live API

---

## Tech Stack

**Backend:** Java 21, Spring Boot 3, Spring Security, Spring Data JPA / Hibernate, MySQL, Maven
**Frontend:** React (Vite), React Router, Axios
**Auth:** JWT (JJWT library), BCrypt password hashing

---

## Architecture

```
Category (1) ──< (many) Product
Product  (1) ──< (many) InventoryTransaction
Product  (1) ──< (many) PurchaseOrderItem
Supplier (1) ──< (many) PurchaseOrder
PurchaseOrder (1) ──< (many) PurchaseOrderItem
PurchaseOrder (1) ──< (many) InventoryTransaction   (nullable reference)
User     (1) ──< (many) InventoryTransaction, PurchaseOrder, UserActivityLog
```

**Layered backend structure**, one clear responsibility per layer:
```
Controller → Service → Repository → Entity
```
Each module (Category, Product, Supplier, PurchaseOrder, InventoryTransaction, UserActivityLog) follows this same shape — separate, explicit service classes per entity rather than a generic base repository/service pattern, prioritizing readability over DRY abstraction for a project at this stage.

---

## Key Design Decisions

A few decisions worth calling out, since they came from deliberate reasoning rather than defaults:

- **Optimistic locking (`@Version`) over pessimistic locking** for stock updates. Conflicting simultaneous writes to the same product are the rare case, not the common one — optimistic locking avoids holding row locks during normal, high-frequency operations, and only pays a cost (retry) when a genuine conflict occurs.
- **`Product.currentQuantity` is denormalized** (not computed by summing transaction history on every read) for fast dashboard/report reads. The trade-off — a source-of-truth duplication risk — is mitigated by wrapping every stock-changing operation in a single `@Transactional` boundary, so the transaction log and the product quantity always update together or not at all.
- **`InventoryTransaction` is one unified table with a `type` discriminator** (STOCK_IN / STOCK_OUT / DAMAGED / LOST / ADJUSTMENT / CORRECTION), not split into separate tables per type — since "show full history for this product" is a constant, frequent query, and all types share the same core shape.
- **Corrections are new, linked rows, never edits** to existing transactions — the audit log is genuinely append-only.
- **Two separate audit logs**, not one: `InventoryTransaction` (what happened to stock — business data) and `UserActivityLog` (who did what, when — security/accountability data, including actions with no stock impact like a login or a category rename).
- **Role stored as an enum on `User`**, not a separate `Role` table — roles are a small, fixed, code-defined set for V1; a dedicated table would be justified only if per-user, fine-grained permissions were needed.

---

## Getting Started

### Prerequisites
- Java 21 (JDK)
- Maven
- MySQL 8+
- Node.js + npm

### Backend
```bash
cd backend
# Copy the template and fill in your local DB password / JWT secret
cp src/main/resources/application.properties.example src/main/resources/application.properties
mvn spring-boot:run
```
The backend runs on `http://localhost:8080`. On first run, Hibernate creates the schema automatically (`ddl-auto=update`).

### Frontend
```bash
cd frontend
npm install
npm run dev
```
The frontend runs on `http://localhost:5173`.

### First login
Register a Staff account via `POST /api/auth/register`, or create an Admin directly in the database and use `/api/auth/login`. Only an existing Admin can create Manager/Admin accounts (via `/api/auth/register-privileged`) — public registration is deliberately locked to the lowest-privilege role.

---

## API Overview

| Area | Base path | Notes |
|---|---|---|
| Auth | `/api/auth` | `register`, `login`, `register-privileged` (Admin only) |
| Categories | `/api/categories` | View: all roles · Write: Admin/Manager |
| Products | `/api/products` | View: all roles · Write: Admin/Manager |
| Suppliers | `/api/suppliers` | View: all roles · Write: Admin/Manager |
| Purchase Orders | `/api/purchase-orders` | Create: Admin/Manager |
| Inventory Transactions | `/api/inventory-transactions` | Stock In/Out: all roles · Approve/Reject: Admin/Manager |
| Activity Logs | `/api/activity-logs` | Admin only |

---

## Security

- Passwords hashed with **BCrypt**, never stored or returned in plain text
- **Stateless JWT authentication** — no server-side sessions; every request authenticates independently via its token
- **Method-level authorization** (`@PreAuthorize`) enforcing role-based access per endpoint, matching the documented permission model
- **CORS** explicitly scoped to the known frontend origin
- Centralized exception handling (`@RestControllerAdvice`) — no raw stack traces or leaked internals in API responses

---

## Roadmap / V2

Deliberately out of scope for V1, to ship a complete, polished system on a deadline rather than an unfinished ambitious one:

- Multi-warehouse support and inter-warehouse transfers
- Partial PO receiving, PO approval workflow, invoice management
- Purchase history by supplier (dedicated report)
- Email / SMS / push notifications for low stock
- Barcode scanning (the `barcode` field already exists on `Product`, schema-ready)
- Supplier/customer login portal
- Automated Docker deployment and CI pipeline
- Test coverage (unit + integration)