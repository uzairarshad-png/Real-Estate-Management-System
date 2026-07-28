# 🏠 Real Estate Management System (REMS)
### 🎓 OOP Lab Project — CSL-210

REMS is a sophisticated, full-stack desktop application built with **JavaFX** and **SQLite**, designed to streamline real-estate operations. It features a triple-role architecture (Admin, Agent, Client) with a focus on robust OOP design, secure data persistence, and a premium, modern user interface.

---

## 🎨 Premium UI/UX Features
REMS stands out with a custom-engineered design system implemented entirely in Java (via `StyleManager`), avoiding external CSS files for maximum performance and portability.

*   **Glassmorphism Effects**: Translucent "Glass Cards" for a modern, airy feel.
*   **Dynamic Gradients**: Premium button styles with interactive hover states and depth-inducing dropshadows.
*   **Role-Specific Dashboards**: Custom-tailored layouts for Admins, Agents, and Clients.
*   **State-Aware UI**: Animated status chips (Active, Pending, Locked) that update in real-time.
*   **Responsive Layouts**: Flexible grid systems for property listings and transaction history.

---

## 📋 Project Architecture & Statistics

The project follows a modular package structure to ensure high maintainability and separation of concerns.

| Package | Count | Responsibilities |
| :--- | :---: | :--- |
| `model` | **17** | Core entities (Property, Person, Transaction) and their hierarchies. |
| `enums` | **10** | Type-safe state management for statuses, roles, and types. |
| `exceptions` | **10** | Custom business logic error handling (Budget, Authentication, etc.). |
| `gui` | **7** | JavaFX controllers and the centralized `StyleManager`. |
| `security` | **6** | Session management, Audit logging, and Authentication. |
| `notification`| **4** | Real-time system alerts and user notification center. |
| `interfaces` | **4** | Functional contracts (Searchable, Transactable, etc.). |
| `payment` | **2** | Transaction processing and digital wallet simulation. |
| `database` | **1** | Singleton DAO for SQLite persistence. |

**Total Java Source Files:** 61

---

## 📦 Deep OOP Implementation

This project serves as a comprehensive demonstration of Advanced Object-Oriented Programming:

1.  **Inheritance & Abstraction**: 
    *   3-level hierarchy for `Property` (Residential/Commercial → House/Apartment/Shop/Office).
    *   `Person` base class inherited by `Admin`, `Agent`, and `Client`.
2.  **Polymorphism**: 
    *   Method overriding for tax calculations and commission rates across different transaction types (`Purchase`, `Rent`, `Sale`).
    *   Interface implementation for searching and processing payments.
3.  **Encapsulation**: 
    *   Strict use of `private` fields with validated `getters`/`setters`.
    *   Data integrity checks within model constructors.
4.  **Design Patterns**:
    *   **Singleton**: Used for `DatabaseManager`, `Session`, `AuditLog`, and `NotificationCenter`.
    *   **DAO Pattern**: Decoupling business logic from persistence.
5.  **Robust Error Handling**: 
    *   Hierarchical custom exceptions extending `REMSException` for granular error catching.

---

## 🔐 System Roles & Access Control

| Role | Key Capabilities |
| :--- | :--- |
| **👑 Admin** | Approve/Reject agent registrations, view global Audit Logs, manage financial overrides. |
| **👔 Agent** | List new properties, manage existing listings, track commissions, process deals. |
| **👤 Client** | Interactive property search, manage personal wallet, purchase/rent properties, track history. |

**Default Admin Credentials:**
*   **Email:** `admin@rems.com`
*   **Password:** `Admin@123`

---

## 🚀 Getting Started

### Prerequisites
*   **JDK 17+** (Recommended: Java 21)
*   **JavaFX SDK** (Required for GUI)
*   **SQLite JDBC Driver** (Required for Database)

### Quick Run (Windows)
We provide a pre-configured `run.bat` script. 
1.  Open `run.bat` in a text editor.
2.  Update the `FX_LIB` and `SQLITE_DIR` paths to match your local installation.
3.  Double-click `run.bat` to compile and launch.

### Manual Compilation
```bash
# Compile
javac --module-path %FX_LIB% --add-modules javafx.controls,javafx.fxml -cp "lib/*" -d bin src/**/*.java

# Run
java --module-path %FX_LIB% --add-modules javafx.controls,javafx.fxml -cp "bin;lib/*" Main
```

---

## 🗄️ Database Schema
The system uses a local SQLite database (`rems.db`). On first launch, the schema is automatically generated with 8 core tables:
*   `clients` / `agents` (User management)
*   `properties` (Listings & details)
*   `transactions` / `payments` (Financial records)
*   `registration_requests` (Admin approval queue)
*   `notifications` / `audit_logs` (System monitoring)

---
*Developed as a high-standard OOP demonstration.*
**Author: Uzair Arshad** 