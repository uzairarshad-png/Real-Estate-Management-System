package database;

import enums.*;
import model.*;
import notification.*;
import payment.Payment;
import payment.PaymentEngine;
import security.AuditLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton DatabaseManager — SQLite3 via JDBC.
 *
 * Manages 8 tables:
 *   clients               — Client profiles
 *   agents                — Agent profiles
 *   properties            — All property listings (all 5 types)
 *   transactions          — All transaction records
 *   payments              — All payment records
 *   registration_requests — Pending/approved/rejected sign-up requests
 *   notifications         — Admin notification queue
 *   audit_logs            — System event log
 *
 * Usage:
 *   DatabaseManager db = DatabaseManager.getInstance();
 *   db.initializeDatabase();   // call once on app startup
 *   db.closeConnection();      // call on app shutdown
 */
public class DatabaseManager {

    // ─── Singleton ────────────────────────────────────────────
    private static DatabaseManager instance;

    // ─── Connection ───────────────────────────────────────────
    public  static final String DB_URL = "jdbc:sqlite:database/rems.db";
    private Connection           connection;
    private final java.util.Map<Integer, Person> userCache = new java.util.HashMap<>();

    // ─── Private Constructor ──────────────────────────────────
    private DatabaseManager() {}

    public static DatabaseManager getInstance() {
        if (instance == null) instance = new DatabaseManager();
        return instance;
    }

    // ══════════════════════════════════════════════════════════
    //  CONNECTION
    // ══════════════════════════════════════════════════════════

    /**
     * Open the SQLite connection and create all tables.
     * Call once on application startup.
     */
    public void initializeDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            connection.setAutoCommit(true);
            createAllTables();
            migrateDatabase();
            System.out.println("[DatabaseManager] Connected to rems.db ✓");
        } catch (ClassNotFoundException e) {
            System.err.println("[DatabaseManager] SQLite JDBC driver not found. "
                + "Add sqlite-jdbc.jar to your classpath.");
            throw new RuntimeException("SQLite JDBC driver missing.", e);
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Failed to connect: " + e.getMessage());
            throw new RuntimeException("Database connection failed.", e);
        }
    }

    /** Close the SQLite connection. Call on application shutdown. */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DatabaseManager] Connection closed ✓");
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Error closing connection: " + e.getMessage());
        }
    }

    /** Check if the database connection is open and valid. */
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    // ══════════════════════════════════════════════════════════
    //  TABLE CREATION
    // ══════════════════════════════════════════════════════════

    private void createAllTables() throws SQLException {
        createClientsTable();
        createAgentsTable();
        createPropertiesTable();
        createTransactionsTable();
        createPaymentsTable();
        createRegistrationRequestsTable();
        createNotificationsTable();
        createAuditLogsTable();
        createOffersTable();
        createChatMessagesTable();
        System.out.println("[DatabaseManager] All 10 tables initialized ✓");
    }

    private void migrateDatabase() {
        // Migration 1: Add image_path to properties if missing
        try (Statement st = connection.createStatement()) {
            st.execute("ALTER TABLE properties ADD COLUMN image_path TEXT;");
            System.out.println("[DatabaseManager] Migration: Added image_path to properties table.");
        } catch (SQLException e) {
            // Error is expected if column already exists in SQLite
            if (!e.getMessage().contains("duplicate column name")) {
                // Silently skip if it's just the 'already exists' error
            }
        }
    }

    private void createClientsTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS clients (
                person_id        INTEGER PRIMARY KEY,
                name             TEXT    NOT NULL,
                email            TEXT    NOT NULL UNIQUE,
                phone            TEXT    NOT NULL,
                cnic             TEXT    NOT NULL UNIQUE,
                password_hash    TEXT    NOT NULL,
                budget           REAL    NOT NULL DEFAULT 0.0,
                wallet_balance   REAL    NOT NULL DEFAULT 0.0,
                preferred_type   TEXT    DEFAULT 'Any',
                account_status   TEXT    NOT NULL DEFAULT 'ACTIVE',
                pending_approval INTEGER NOT NULL DEFAULT 0
            );
            """;
        execute(sql);
    }

    private void createAgentsTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS agents (
                person_id        INTEGER PRIMARY KEY,
                name             TEXT    NOT NULL,
                email            TEXT    NOT NULL UNIQUE,
                phone            TEXT    NOT NULL,
                cnic             TEXT    NOT NULL UNIQUE,
                password_hash    TEXT    NOT NULL,
                license_number   TEXT    NOT NULL UNIQUE,
                monthly_salary   REAL    NOT NULL DEFAULT 0.0,
                commission_rate  REAL    NOT NULL DEFAULT 0.0,
                total_earnings   REAL    NOT NULL DEFAULT 0.0,
                wallet_balance   REAL    NOT NULL DEFAULT 0.0,
                account_status   TEXT    NOT NULL DEFAULT 'ACTIVE'
            );
            """;
        execute(sql);
    }

    private void createPropertiesTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS properties (
                property_id        INTEGER PRIMARY KEY,
                property_type      TEXT    NOT NULL,
                title              TEXT    NOT NULL,
                address            TEXT    NOT NULL,
                city               TEXT    NOT NULL,
                area               REAL    NOT NULL,
                base_price         REAL    NOT NULL,
                status             TEXT    NOT NULL DEFAULT 'UNDER_REVIEW',
                mode               TEXT    NOT NULL,
                assigned_agent_id  INTEGER REFERENCES agents(person_id),
                owner_id           INTEGER REFERENCES clients(person_id),
                -- Residential fields
                bedrooms           INTEGER,
                bathrooms          INTEGER,
                is_furnished       INTEGER,
                -- Apartment specific
                floor_number       INTEGER,
                has_elevator       INTEGER,
                has_parking        INTEGER,
                -- House specific
                has_garage         INTEGER,
                garden_area        REAL,
                floors             INTEGER,
                -- Plot specific
                plot_type          TEXT,
                is_corner_plot     INTEGER,
                is_on_main_road    INTEGER,
                facing             TEXT,
                -- Commercial fields
                business_zone      TEXT,
                parking_spots      INTEGER,
                -- Office specific
                workstations       INTEGER,
                has_conference_room INTEGER,
                -- Shop specific
                shop_category      TEXT,
                has_storage_room   INTEGER,
                image_path         TEXT
            );
            """;
        execute(sql);
    }

    private void createTransactionsTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS transactions (
                transaction_id   INTEGER PRIMARY KEY,
                transaction_type TEXT    NOT NULL,
                property_id      INTEGER NOT NULL REFERENCES properties(property_id),
                client_id        INTEGER NOT NULL REFERENCES clients(person_id),
                agent_id         INTEGER REFERENCES agents(person_id),
                amount           REAL    NOT NULL,
                date             TEXT    NOT NULL,
                status           TEXT    NOT NULL DEFAULT 'PENDING',
                extra_data       TEXT
            );
            """;
        execute(sql);
    }

    private void createPaymentsTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS payments (
                payment_id              INTEGER PRIMARY KEY,
                payment_type            TEXT    NOT NULL,
                amount                  REAL    NOT NULL,
                payer                   TEXT    NOT NULL,
                payee                   TEXT    NOT NULL,
                status                  TEXT    NOT NULL DEFAULT 'PENDING_ADMIN',
                timestamp               TEXT    NOT NULL,
                related_transaction_id  INTEGER DEFAULT -1,
                admin_note              TEXT    DEFAULT ''
            );
            """;
        execute(sql);
    }

    private void createRegistrationRequestsTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS registration_requests (
                request_id      INTEGER PRIMARY KEY,
                name            TEXT    NOT NULL,
                email           TEXT    NOT NULL,
                phone           TEXT    NOT NULL,
                cnic            TEXT    NOT NULL,
                password_hash   TEXT    NOT NULL,
                requested_role  TEXT    NOT NULL,
                source          TEXT    NOT NULL,
                submitted_at    TEXT    NOT NULL,
                status          TEXT    NOT NULL DEFAULT 'PENDING'
            );
            """;
        execute(sql);
    }

    private void createNotificationsTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS notifications (
                notif_id        INTEGER PRIMARY KEY,
                type            TEXT    NOT NULL,
                message         TEXT    NOT NULL,
                related_email   TEXT,
                timestamp       TEXT    NOT NULL,
                is_read         INTEGER NOT NULL DEFAULT 0
            );
            """;
        execute(sql);
    }

    private void createAuditLogsTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS audit_logs (
                log_id      INTEGER PRIMARY KEY AUTOINCREMENT,
                entry       TEXT    NOT NULL,
                logged_at   TEXT    NOT NULL DEFAULT (datetime('now','localtime'))
            );
            """;
        execute(sql);
    }

    private void createOffersTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS offers (
                offer_id     INTEGER PRIMARY KEY,
                property_id  INTEGER NOT NULL REFERENCES properties(property_id),
                offerer_id   INTEGER NOT NULL REFERENCES clients(person_id),
                receiver_id  INTEGER NOT NULL REFERENCES persons(person_id),
                amount       REAL    NOT NULL,
                status       TEXT    NOT NULL,
                timestamp    TEXT    NOT NULL
            );
            """;
        execute(sql);
    }

    private void createChatMessagesTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS chat_messages (
                message_id   INTEGER PRIMARY KEY,
                sender_id    INTEGER NOT NULL,
                receiver_id  INTEGER NOT NULL,
                content      TEXT    NOT NULL,
                timestamp    TEXT    NOT NULL,
                is_read      INTEGER NOT NULL DEFAULT 0
            );
            """;
        execute(sql);
    }

    // ══════════════════════════════════════════════════════════
    //  CLIENTS — SAVE / LOAD / UPDATE
    // ══════════════════════════════════════════════════════════

    /** Insert or replace a Client record. */
    public void saveClient(Client client) {
        if (client == null) return;
        String sql = """
            INSERT OR REPLACE INTO clients
            (person_id, name, email, phone, cnic, password_hash,
             budget, wallet_balance, preferred_type, account_status, pending_approval)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt   (1, client.getPersonId());
            ps.setString(2, client.getName());
            ps.setString(3, client.getEmail());
            ps.setString(4, client.getPhone());
            ps.setString(5, client.getCnic());
            ps.setString(6, client.getPasswordHash());
            ps.setDouble(7, client.getBudget());
            ps.setDouble(8, client.getWalletBalance());
            ps.setString(9, client.getPreferredPropertyType());
            ps.setString(10, client.getAccountStatus().name());
            ps.setInt   (11, client.isPendingAdminApproval() ? 1 : 0);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] Error saving client: " + e.getMessage());
        }
    }

    /** Load all Client records from database. */
    public List<Client> loadAllClients() {
        List<Client> clients = new ArrayList<>();
        String sql = "SELECT * FROM clients;";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Client c = new Client(
                    rs.getInt("person_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("cnic"),
                    rs.getString("password_hash"),
                    rs.getDouble("budget")
                );
                c.setWalletBalance(rs.getDouble("wallet_balance"));
                c.setPreferredPropertyType(rs.getString("preferred_type"));
                c.setAccountStatus(AccountStatus.valueOf(rs.getString("account_status")));
                clients.add(c);
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error loading clients: " + e.getMessage());
        }
        return clients;
    }

    // ══════════════════════════════════════════════════════════
    //  AGENTS — SAVE / LOAD / UPDATE
    // ══════════════════════════════════════════════════════════

    /** Insert or replace an Agent record. */
    public void saveAgent(Agent agent) {
        if (agent == null) return;
        String sql = """
            INSERT OR REPLACE INTO agents
            (person_id, name, email, phone, cnic, password_hash,
             license_number, monthly_salary, commission_rate,
             total_earnings, wallet_balance, account_status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt   (1,  agent.getPersonId());
            ps.setString(2,  agent.getName());
            ps.setString(3,  agent.getEmail());
            ps.setString(4,  agent.getPhone());
            ps.setString(5,  agent.getCnic());
            ps.setString(6,  agent.getPasswordHash());
            ps.setString(7,  agent.getLicenseNumber());
            ps.setDouble(8,  agent.getMonthlySalary());
            ps.setDouble(9,  agent.getCommissionRate());
            ps.setDouble(10, agent.getTotalEarnings());
            ps.setDouble(11, agent.getWalletBalance());
            ps.setString(12, agent.getAccountStatus().name());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] Error saving agent: " + e.getMessage());
        }
    }

    /** Load all Agent records from database. */
    public List<Agent> loadAllAgents() {
        List<Agent> agents = new ArrayList<>();
        String sql = "SELECT * FROM agents;";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Agent a = new Agent(
                    rs.getInt("person_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("cnic"),
                    rs.getString("password_hash"),
                    rs.getString("license_number"),
                    rs.getDouble("monthly_salary"),
                    rs.getDouble("commission_rate")
                );
                a.setTotalEarnings(rs.getDouble("total_earnings"));
                a.setWalletBalance(rs.getDouble("wallet_balance"));
                a.setAccountStatus(AccountStatus.valueOf(rs.getString("account_status")));
                agents.add(a);
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error loading agents: " + e.getMessage());
        }
        return agents;
    }

    // ══════════════════════════════════════════════════════════
    //  PROPERTIES — SAVE / LOAD
    // ══════════════════════════════════════════════════════════

    /** Insert or replace a Property record. Handles all 5 property types. */
    public boolean saveProperty(Property property) {
        if (property == null) return false;
        String sql = """
            INSERT OR REPLACE INTO properties
            (property_id, property_type, title, address, city, area, base_price,
             status, mode, assigned_agent_id, owner_id,
             bedrooms, bathrooms, is_furnished,
             floor_number, has_elevator, has_parking,
             has_garage, garden_area, floors,
             plot_type, is_corner_plot, is_on_main_road, facing,
             business_zone, parking_spots,
             workstations, has_conference_room,
             shop_category, has_storage_room, image_path)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt   (1,  property.getPropertyId());
            ps.setString(2,  property.getPropertyType());
            ps.setString(3,  property.getTitle());
            ps.setString(4,  property.getAddress());
            ps.setString(5,  property.getCity());
            ps.setDouble(6,  property.getArea());
            ps.setDouble(7,  property.getBasePrice());
            ps.setString(8,  property.getStatus().name());
            ps.setString(9,  property.getMode().name());
            if (property.getAssignedAgent() != null) {
                ps.setInt(10, property.getAssignedAgent().getPersonId());
            } else { ps.setNull(10, java.sql.Types.INTEGER); }

            if (property.getOwner() != null) {
                ps.setInt(11, property.getOwner().getPersonId());
            } else { ps.setNull(11, java.sql.Types.INTEGER); }

            // Residential fields
            if (property instanceof ResidentialProperty rp) {
                ps.setInt(12, rp.getBedrooms());
                ps.setInt(13, rp.getBathrooms());
                ps.setInt(14, rp.isFurnished() ? 1 : 0);
            } else { ps.setNull(12, Types.INTEGER);
                     ps.setNull(13, Types.INTEGER);
                     ps.setNull(14, Types.INTEGER); }

            // Apartment
            if (property instanceof Apartment ap) {
                ps.setInt(15, ap.getFloorNumber());
                ps.setInt(16, ap.hasElevator() ? 1 : 0);
                ps.setInt(17, ap.hasParking()  ? 1 : 0);
            } else { ps.setNull(15, Types.INTEGER);
                     ps.setNull(16, Types.INTEGER);
                     ps.setNull(17, Types.INTEGER); }

            // House
            if (property instanceof House h) {
                ps.setInt   (18, h.hasGarage() ? 1 : 0);
                ps.setDouble(19, h.getGardenArea());
                ps.setInt   (20, h.getFloors());
            } else { ps.setNull(18, Types.INTEGER);
                     ps.setNull(19, Types.REAL);
                     ps.setNull(20, Types.INTEGER); }

            // Plot
            if (property instanceof Plot pl) {
                ps.setString(21, pl.getPlotType());
                ps.setInt   (22, pl.isCornerPlot()  ? 1 : 0);
                ps.setInt   (23, pl.isOnMainRoad()   ? 1 : 0);
                ps.setString(24, pl.getFacing());
            } else { ps.setNull(21, Types.VARCHAR);
                     ps.setNull(22, Types.INTEGER);
                     ps.setNull(23, Types.INTEGER);
                     ps.setNull(24, Types.VARCHAR); }

            // Commercial base
            if (property instanceof CommercialProperty cp) {
                ps.setString(25, cp.getBusinessZone());
                ps.setInt   (26, cp.getParkingSpots());
            } else { ps.setNull(25, Types.VARCHAR);
                     ps.setNull(26, Types.INTEGER); }

            // Office
            if (property instanceof Office o) {
                ps.setInt(27, o.getWorkstations());
                ps.setInt(28, o.hasConferenceRoom() ? 1 : 0);
            } else { ps.setNull(27, Types.INTEGER);
                     ps.setNull(28, Types.INTEGER); }

            // Shop
            if (property instanceof Shop s) {
                ps.setString(29, s.getShopCategory());
                ps.setInt   (30, s.hasStorageRoom() ? 1 : 0);
            } else { ps.setNull(29, Types.VARCHAR);
                     ps.setNull(30, Types.INTEGER); }

            ps.setString(31, property.getImagePath());

            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("[DB] Error saving property: " + e.getMessage());
            return false;
        }
    }

    /** Load all Property records. Returns base Property list; caller casts as needed. */
    public List<Property> loadAllProperties() {
        List<Property> properties = new ArrayList<>();
        String sql = "SELECT * FROM properties;";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Property p = buildPropertyFromRow(rs);
                if (p != null) properties.add(p);
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error loading properties: " + e.getMessage());
        }
        return properties;
    }

    /** Reconstruct a Property object from a ResultSet row. */
    private Property buildPropertyFromRow(ResultSet rs) throws SQLException {
        String type     = rs.getString("property_type");
        int    id       = rs.getInt("property_id");
        String title    = rs.getString("title");
        String address  = rs.getString("address");
        String city     = rs.getString("city");
        double area     = rs.getDouble("area");
        double price    = rs.getDouble("base_price");
        PropertyMode mode = PropertyMode.valueOf(rs.getString("mode"));

        Property p = switch (type) {
            case "Apartment" -> new Apartment(id, title, address, city, area, price, mode,
                rs.getInt("bedrooms"), rs.getInt("bathrooms"),
                rs.getInt("is_furnished") == 1,
                rs.getInt("floor_number"),
                rs.getInt("has_elevator") == 1,
                rs.getInt("has_parking")  == 1);

            case "House" -> new House(id, title, address, city, area, price, mode,
                rs.getInt("bedrooms"), rs.getInt("bathrooms"),
                rs.getInt("is_furnished") == 1,
                rs.getInt("has_garage")   == 1,
                rs.getDouble("garden_area"),
                rs.getInt("floors"));

            case "Plot" -> new Plot(id, title, address, city, area, price, mode,
                rs.getString("plot_type"),
                rs.getInt("is_corner_plot")  == 1,
                rs.getInt("is_on_main_road") == 1,
                rs.getString("facing"));

            case "Office" -> new Office(id, title, address, city, area, price, mode,
                rs.getString("business_zone"),
                rs.getInt("parking_spots"),
                rs.getInt("workstations"),
                rs.getInt("has_conference_room") == 1);

            case "Shop" -> new Shop(id, title, address, city, area, price, mode,
                rs.getString("business_zone"),
                rs.getInt("parking_spots"),
                rs.getString("shop_category"),
                rs.getInt("has_storage_room") == 1);

            default -> null;
        };

        if (p != null) {
            p.setStatus(PropertyStatus.valueOf(rs.getString("status")));
            p.setImagePath(rs.getString("image_path"));
            
            int agentId = rs.getInt("assigned_agent_id");
            if (agentId > 0) {
                Person agent = findById(agentId);
                if (agent instanceof Agent a) p.setAssignedAgent(a);
            }
            
            int ownerId = rs.getInt("owner_id");
            if (ownerId > 0) {
                Person owner = findById(ownerId);
                if (owner instanceof Client c) p.setOwner(c);
            }
        }
        return p;
    }

    // ══════════════════════════════════════════════════════════
    //  TRANSACTIONS — SAVE / LOAD
    // ══════════════════════════════════════════════════════════

    /** Save a Transaction record. */
    public void saveTransaction(Transaction transaction) {
        if (transaction == null) return;
        String sql = """
            INSERT OR REPLACE INTO transactions
            (transaction_id, transaction_type, property_id, client_id,
             agent_id, amount, date, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?);
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt   (1, transaction.getTransactionId());
            ps.setString(2, transaction.getTransactionType());
            ps.setInt   (3, transaction.getProperty().getPropertyId());
            ps.setInt   (4, transaction.getClient().getPersonId());
            ps.setObject(5, transaction.getAgent() != null
                            ? transaction.getAgent().getPersonId() : null);
            ps.setDouble(6, transaction.getAmount());
            ps.setString(7, transaction.getDate());
            ps.setString(8, transaction.getStatus().name());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] Error saving transaction: " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════
    //  PAYMENTS — SAVE / LOAD
    // ══════════════════════════════════════════════════════════

    /** Save a Payment record. */
    public void savePayment(Payment payment) {
        if (payment == null) return;
        String sql = """
            INSERT OR REPLACE INTO payments
            (payment_id, payment_type, amount, payer, payee,
             status, timestamp, related_transaction_id, admin_note)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt   (1, payment.getPaymentId());
            ps.setString(2, payment.getType().name());
            ps.setDouble(3, payment.getAmount());
            ps.setString(4, payment.getPayer());
            ps.setString(5, payment.getPayee());
            ps.setString(6, payment.getStatus().name());
            ps.setString(7, payment.getTimestamp());
            ps.setInt   (8, payment.getRelatedTransactionId());
            ps.setString(9, payment.getAdminNote());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] Error saving payment: " + e.getMessage());
        }
    }

    /** Load all Payment records. */
    public List<Payment> loadAllPayments() {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM payments;";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Payment p = new Payment(
                    PaymentType.valueOf(rs.getString("payment_type")),
                    rs.getDouble("amount"),
                    rs.getString("payer"),
                    rs.getString("payee"),
                    rs.getInt("related_transaction_id")
                );
                // Restore status
                String savedStatus = rs.getString("status");
                switch (PaymentStatus.valueOf(savedStatus)) {
                    case APPROVED  -> p.approve();
                    case REJECTED  -> p.reject(rs.getString("admin_note"));
                    case DISBURSED -> { p.approve(); p.disburse(); }
                    default        -> { /* PENDING_ADMIN — already default */ }
                }
                payments.add(p);
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error loading payments: " + e.getMessage());
        }
        return payments;
    }

    // ══════════════════════════════════════════════════════════
    //  REGISTRATION REQUESTS — SAVE / LOAD
    // ══════════════════════════════════════════════════════════

    /** Save a RegistrationRequest record. */
    public void saveRegistrationRequest(RegistrationRequest req) {
        if (req == null) return;
        String sql = """
            INSERT OR REPLACE INTO registration_requests
            (request_id, name, email, phone, cnic, password_hash,
             requested_role, source, submitted_at, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt   (1, req.getRequestId());
            ps.setString(2, req.getName());
            ps.setString(3, req.getEmail());
            ps.setString(4, req.getPhone());
            ps.setString(5, req.getCnic());
            ps.setString(6, req.getPasswordHash());
            ps.setString(7, req.getRequestedRole().name());
            ps.setString(8, req.getSource().name());
            ps.setString(9, req.getSubmittedAt());
            ps.setString(10, req.getStatus().name());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] Error saving registration request: " + e.getMessage());
        }
    }

    /** Load all PENDING registration requests. */
    public List<RegistrationRequest> loadPendingRequests() {
        List<RegistrationRequest> requests = new ArrayList<>();
        String sql = "SELECT * FROM registration_requests WHERE status = 'PENDING';";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                RegistrationRequest req = new RegistrationRequest(
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("cnic"),
                    rs.getString("password_hash"),
                    UserRole.valueOf(rs.getString("requested_role")),
                    RegistrationSource.valueOf(rs.getString("source"))
                );
                requests.add(req);
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error loading registration requests: " + e.getMessage());
        }
        return requests;
    }

    // ══════════════════════════════════════════════════════════
    //  NOTIFICATIONS — SAVE / LOAD
    // ══════════════════════════════════════════════════════════

    /** Save a Notification record. */
    public void saveNotification(notification.Notification notif) {
        if (notif == null) return;
        String sql = """
            INSERT OR REPLACE INTO notifications
            (notif_id, type, message, related_email, timestamp, is_read)
            VALUES (?, ?, ?, ?, ?, ?);
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt   (1, notif.getNotifId());
            ps.setString(2, notif.getType().name());
            ps.setString(3, notif.getMessage());
            ps.setString(4, notif.getRelatedEmail());
            ps.setString(5, notif.getTimestamp());
            ps.setInt   (6, notif.isRead() ? 1 : 0);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] Error saving notification: " + e.getMessage());
        }
    }

    /** Load all Notification records. */
    public List<notification.Notification> loadNotifications() {
        List<notification.Notification> notifs = new ArrayList<>();
        String sql = "SELECT * FROM notifications ORDER BY notif_id ASC;";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                notification.Notification n = new notification.Notification(
                    NotificationType.valueOf(rs.getString("type")),
                    rs.getString("message"),
                    rs.getString("related_email")
                );
                if (rs.getInt("is_read") == 1) n.markAsRead();
                notifs.add(n);
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error loading notifications: " + e.getMessage());
        }
        return notifs;
    }

    // ══════════════════════════════════════════════════════════
    //  AUDIT LOGS — SAVE / LOAD
    // ══════════════════════════════════════════════════════════

    /** Save a single audit log entry. */
    public void saveAuditLog(String entry) {
        if (entry == null || entry.trim().isEmpty()) return;
        String sql = "INSERT INTO audit_logs (entry) VALUES (?);";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, entry.trim());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] Error saving audit log: " + e.getMessage());
        }
    }

    /** Load all audit log entries. */
    public List<String> loadAuditLogs() {
        List<String> logs = new ArrayList<>();
        String sql = "SELECT entry FROM audit_logs ORDER BY log_id ASC;";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                logs.add(rs.getString("entry"));
        } catch (SQLException e) {
            System.err.println("[DB] Error loading audit logs: " + e.getMessage());
        }
        return logs;
    }

    // ══════════════════════════════════════════════════════════
    //  UPDATE METHODS
    // ══════════════════════════════════════════════════════════

    /** Update a user's AccountStatus (clients + agents). */
    public void updateAccountStatus(String email, AccountStatus status) {
        if (email == null || status == null) return;
        // Try clients table first
        updateField("clients", "account_status", status.name(), "email", email);
        // Then agents table
        updateField("agents",  "account_status", status.name(), "email", email);
    }

    /** Update a RegistrationRequest status by email. */
    public void updateRequestStatus(String email, RequestStatus status) {
        if (email == null || status == null) return;
        updateField("registration_requests", "status", status.name(), "email", email);
    }

    // ─── OFFERS ───

    public void saveOffer(Offer offer) {
        String sql = "INSERT OR REPLACE INTO offers (offer_id, property_id, offerer_id, receiver_id, amount, status, timestamp) VALUES (?, ?, ?, ?, ?, ?, ?);";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, offer.getOfferId());
            ps.setInt(2, offer.getPropertyId());
            ps.setInt(3, offer.getOffererId());
            ps.setInt(4, offer.getReceiverId());
            ps.setDouble(5, offer.getAmount());
            ps.setString(6, offer.getStatus().name());
            ps.setString(7, offer.getTimestamp());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] Error saving offer: " + e.getMessage());
        }
    }

    public List<Offer> loadOffersForUser(int userId) {
        List<Offer> offers = new ArrayList<>();
        String sql = "SELECT * FROM offers WHERE offerer_id = ? OR receiver_id = ? ORDER BY timestamp DESC;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Offer o = new Offer(rs.getInt("offer_id"), rs.getInt("property_id"), rs.getInt("offerer_id"), rs.getInt("receiver_id"), rs.getDouble("amount"));
                o.setStatus(OfferStatus.valueOf(rs.getString("status")));
                o.setTimestamp(rs.getString("timestamp"));
                offers.add(o);
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error loading offers: " + e.getMessage());
        }
        return offers;
    }

    public void updateOfferStatus(int offerId, OfferStatus status) {
        String sql = "UPDATE offers SET status = ? WHERE offer_id = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, offerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] Error updating offer status: " + e.getMessage());
        }
    }

    // ─── CHAT ───

    public void saveChatMessage(ChatMessage msg) {
        String sql = "INSERT OR REPLACE INTO chat_messages (message_id, sender_id, receiver_id, content, timestamp, is_read) VALUES (?, ?, ?, ?, ?, ?);";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, msg.getMessageId());
            ps.setInt(2, msg.getSenderId());
            ps.setInt(3, msg.getReceiverId());
            ps.setString(4, msg.getContent());
            ps.setString(5, msg.getTimestamp());
            ps.setInt(6, msg.isRead() ? 1 : 0);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] Error saving chat message: " + e.getMessage());
        }
    }

    public List<ChatMessage> loadChatHistory(int u1, int u2) {
        List<ChatMessage> msgs = new ArrayList<>();
        String sql = "SELECT * FROM chat_messages WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?) ORDER BY timestamp ASC;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, u1); ps.setInt(2, u2);
            ps.setInt(3, u2); ps.setInt(4, u1);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ChatMessage m = new ChatMessage(rs.getInt("message_id"), rs.getInt("sender_id"), rs.getInt("receiver_id"), rs.getString("content"));
                m.setTimestamp(rs.getString("timestamp"));
                m.setRead(rs.getInt("is_read") == 1);
                msgs.add(m);
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error loading chat history: " + e.getMessage());
        }
        return msgs;
    }

    public List<Integer> loadRecentChatPartners(int userId) {
        List<Integer> partners = new ArrayList<>();
        String sql = "SELECT DISTINCT sender_id FROM chat_messages WHERE receiver_id = ? UNION SELECT DISTINCT receiver_id FROM chat_messages WHERE sender_id = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) partners.add(rs.getInt(1));
        } catch (SQLException e) {
            System.err.println("[DB] Error loading chat partners: " + e.getMessage());
        }
        return partners;
    }

    public int getNextOfferId() {
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT MAX(offer_id) FROM offers;")) {
            if (rs.next()) return rs.getInt(1) + 1;
        } catch (SQLException e) {}
        return 1;
    }

    public int getNextMessageId() {
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT MAX(message_id) FROM chat_messages;")) {
            if (rs.next()) return rs.getInt(1) + 1;
        } catch (SQLException e) {}
        return 1;
    }

    /** Update a user's password hash. */
    public void updatePasswordHash(String email, String newHash) {
        if (email == null || newHash == null) return;
        updateField("clients", "password_hash", newHash, "email", email);
        updateField("agents",  "password_hash", newHash, "email", email);
    }

    /** Update a property's status. */
    public void updatePropertyStatus(int propertyId, PropertyStatus status) {
        if (status == null) return;
        String sql = "UPDATE properties SET status = ? WHERE property_id = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt   (2, propertyId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] Error updating property status: " + e.getMessage());
        }
    }

    /** Update a payment's status. */
    public void updatePaymentStatus(int paymentId, PaymentStatus status) {
        if (status == null) return;
        String sql = "UPDATE payments SET status = ? WHERE payment_id = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt   (2, paymentId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] Error updating payment status: " + e.getMessage());
        }
    }

    /** Update an agent's wallet balance and total earnings. */
    public void updateAgentBalance(int agentId, double walletBalance, double totalEarnings) {
        String sql = "UPDATE agents SET wallet_balance = ?, total_earnings = ? WHERE person_id = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDouble(1, walletBalance);
            ps.setDouble(2, totalEarnings);
            ps.setInt   (3, agentId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] Error updating agent balance: " + e.getMessage());
        }
    }

    /** Update a client's wallet balance. */
    public void updateClientBalance(int clientId, double walletBalance) {
        String sql = "UPDATE clients SET wallet_balance = ? WHERE person_id = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDouble(1, walletBalance);
            ps.setInt   (2, clientId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] Error updating client balance: " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════
    //  LOOKUP METHODS
    // ══════════════════════════════════════════════════════════

    /**
     * Find a Person (Client or Agent) by email.
     * Searches clients table first, then agents.
     * @return Person object or null if not found
     */
    public Person findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) return null;
        String normalizedEmail = email.trim().toLowerCase();

        // Search clients
        String sql = "SELECT * FROM clients WHERE email = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, normalizedEmail);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return findById(rs.getInt("person_id"));
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error in findByEmail (clients): " + e.getMessage());
        }

        // Search agents
        sql = "SELECT * FROM agents WHERE email = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, normalizedEmail);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return findById(rs.getInt("person_id"));
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error in findByEmail (agents): " + e.getMessage());
        }
        return null;
    }

    public Person findById(int id) {
        if (id <= 0) return new Admin();
        if (userCache.containsKey(id)) return userCache.get(id);
        
        // Search clients
        String sql = "SELECT * FROM clients WHERE person_id = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Client c = new Client(rs.getInt("person_id"), rs.getString("name"), rs.getString("email"), rs.getString("phone"), rs.getString("cnic"), rs.getString("password_hash"), rs.getDouble("budget"));
                c.setWalletBalance(rs.getDouble("wallet_balance"));
                c.setAccountStatus(AccountStatus.valueOf(rs.getString("account_status")));
                userCache.put(id, c);
                loadUserProperties(c);
                return c;
            }
        } catch (SQLException e) {}

        // Search agents
        sql = "SELECT * FROM agents WHERE person_id = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Agent a = new Agent(rs.getInt("person_id"), rs.getString("name"), rs.getString("email"), rs.getString("phone"), rs.getString("cnic"), rs.getString("password_hash"), rs.getString("license_number"), rs.getDouble("monthly_salary"), rs.getDouble("commission_rate"));
                a.setTotalEarnings(rs.getDouble("total_earnings"));
                a.setWalletBalance(rs.getDouble("wallet_balance"));
                a.setAccountStatus(AccountStatus.valueOf(rs.getString("account_status")));
                userCache.put(id, a);
                loadUserProperties(a);
                return a;
            }
        } catch (SQLException e) {}

        return null;
    }

    /**
     * Check if an email already exists in clients or agents tables.
     * Used during registration to prevent duplicate accounts.
     */
    public boolean emailExists(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        String normalized = email.trim().toLowerCase();
        String sqlC = "SELECT 1 FROM clients WHERE email = ?;";
        String sqlA = "SELECT 1 FROM agents  WHERE email = ?;";
        try {
            try (PreparedStatement ps = connection.prepareStatement(sqlC)) {
                ps.setString(1, normalized);
                if (ps.executeQuery().next()) return true;
            }
            try (PreparedStatement ps = connection.prepareStatement(sqlA)) {
                ps.setString(1, normalized);
                if (ps.executeQuery().next()) return true;
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error in emailExists: " + e.getMessage());
        }
        return false;
    }

    /**
     * Get the next available auto-increment person_id.
     */
    public int getNextPersonId() {
        int maxClient = 0, maxAgent = 0;
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("SELECT MAX(person_id) FROM clients;");
            if (rs.next()) maxClient = rs.getInt(1);
            rs = st.executeQuery("SELECT MAX(person_id) FROM agents;");
            if (rs.next()) maxAgent = rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[DB] Error getting next person ID: " + e.getMessage());
        }
        return Math.max(maxClient, maxAgent) + 1;
    }

    /**
     * Get the next available property_id.
     */
    public int getNextPropertyId() {
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("SELECT MAX(property_id) FROM properties;");
            if (rs.next()) return rs.getInt(1) + 1;
        } catch (SQLException e) {
            System.err.println("[DB] Error getting next property ID: " + e.getMessage());
        }
        return 1;
    }

    /**
     * Get the next available transaction_id.
     */
    public int getNextTransactionId() {
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("SELECT MAX(transaction_id) FROM transactions;");
            if (rs.next()) return rs.getInt(1) + 1;
        } catch (SQLException e) {
            System.err.println("[DB] Error getting next transaction ID: " + e.getMessage());
        }
        return 1;
    }

    /** Permanently delete a person from the database. */
    public void deletePerson(int personId, String table) {
        String sql = "DELETE FROM " + table + " WHERE person_id = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, personId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] Error deleting from " + table + ": " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════
    //  PRIVATE HELPERS
    // ══════════════════════════════════════════════════════════

    /** Generic field update helper. */
    private void updateField(String table, String column,
                              String value, String whereCol, String whereVal) {
        String sql = "UPDATE " + table + " SET " + column
                   + " = ? WHERE " + whereCol + " = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, value);
            ps.setString(2, whereVal);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] Error updating " + table
                + "." + column + ": " + e.getMessage());
        }
    }

    /** Helper to load and link properties for a user. */
    private void loadUserProperties(Person p) {
        String col = (p instanceof Client) ? "owner_id" : "assigned_agent_id";
        String sql = "SELECT * FROM properties WHERE " + col + " = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, p.getPersonId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Property prop = buildPropertyFromRow(rs);
                if (prop != null) {
                    if (p instanceof Client c) c.addOwnedProperty(prop);
                    else if (p instanceof Agent a) a.addProperty(prop);
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error loading user properties: " + e.getMessage());
        }
    }

    /** Execute a raw DDL SQL statement. */
    private void execute(String sql) throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.execute(sql);
        }
    }
}
