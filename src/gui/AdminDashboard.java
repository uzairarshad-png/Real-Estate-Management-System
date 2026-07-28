package gui;

import database.DatabaseManager;
import enums.*;
import model.*;
import notification.*;
import payment.*;
import security.*;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

import java.util.List;

/**
 * Premium Admin Dashboard — Redesigned with Java-based styling.
 * Added: Create Profile feature for Agents and Clients.
 */
public class AdminDashboard {

    private final Stage           stage;
    private final Admin           admin;
    private final DatabaseManager db;
    private final LoginAttemptTracker tracker;

    private Button activeBtn;
    private VBox contentArea;

    public AdminDashboard(Stage stage) {
        this.stage   = stage;
        this.admin   = new Admin();
        this.db      = DatabaseManager.getInstance();
        this.tracker = new LoginAttemptTracker();
    }

    public void show() {
        stage.setTitle("REMS — Admin Command Center");
        stage.setWidth(1280);
        stage.setHeight(800);
        stage.setResizable(true); // Feature: Window Control

        BorderPane root = new BorderPane();
        root.setLeft(buildSidebar());
        root.setCenter(buildMainArea());

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();

        showOverview();
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox(0);
        sidebar.setStyle(StyleManager.sidebar());
        sidebar.setPrefWidth(260);

        VBox brand = new VBox(5);
        brand.setPadding(new Insets(30, 25, 30, 25));
        brand.setAlignment(Pos.CENTER_LEFT);
        Label icon = new Label("💎 REMS");
        icon.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label roleTag = new Label("ADMINISTRATOR");
        roleTag.setStyle("-fx-font-size: 11px; -fx-text-fill: " + StyleManager.COLOR_ACCENT + "; -fx-letter-spacing: 2;");
        brand.getChildren().addAll(icon, roleTag);

        VBox navBox = new VBox(2);
        navBox.getChildren().addAll(
            navBtn("📊  Overview",        () -> showOverview()),
            navBtn("👤  Create Profile",   () -> showCreateProfile()), // New Feature
            navBtn("📋  Registrations",   () -> showRegistrations()),
            navBtn("🏠  Properties",      () -> showProperties()),
            navBtn("💰  Payments",        () -> showPayments()),
            navBtn("👥  Clients",         () -> showClients()),
            navBtn("🤝  Agents",          () -> showAgents()),
            navBtn("🔔  Notifications",   () -> showNotifications()),
            navBtn("💬  Messages",        () -> showMessages()),
            navBtn("📜  Audit Log",       () -> showAuditLog())
        );

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = new Button("🚪  Sign Out");
        logoutBtn.setStyle(StyleManager.sidebarButton());
        logoutBtn.setPrefWidth(260);
        logoutBtn.setOnAction(e -> handleLogout());

        sidebar.getChildren().addAll(brand, new Separator(), navBox, spacer, logoutBtn);
        return sidebar;
    }

    private Button navBtn(String text, Runnable action) {
        Button btn = new Button(text);
        btn.setStyle(StyleManager.sidebarButton());
        btn.setPrefWidth(260);
        btn.setOnAction(e -> {
            setActiveBtn(btn);
            action.run();
        });
        return btn;
    }

    private void setActiveBtn(Button btn) {
        if (activeBtn != null) activeBtn.setStyle(StyleManager.sidebarButton());
        activeBtn = btn;
        btn.setStyle(StyleManager.sidebarButtonActive());
    }

    private BorderPane buildMainArea() {
        BorderPane main = new BorderPane();
        contentArea = new VBox(25);
        contentArea.setStyle(StyleManager.mainContent());

        ScrollPane scroll = new ScrollPane(contentArea);
        scroll.setFitToWidth(true);
        scroll.setStyle(StyleManager.scrollPane());

        main.setCenter(scroll);
        return main;
    }

    private void setContent(javafx.scene.Node... nodes) {
        contentArea.getChildren().clear();
        contentArea.getChildren().addAll(nodes);
    }

    // ─── Overview Redesign ───
    private void showOverview() {
        Label title = sectionTitle("System Intelligence Overview");
        
        FlowPane cards = new FlowPane(20, 20);
        cards.getChildren().addAll(
            premiumStatCard("Clients", String.valueOf(db.loadAllClients().size()), "👤", "#3498db"),
            premiumStatCard("Agents", String.valueOf(db.loadAllAgents().size()), "🤝", "#2ecc71"),
            premiumStatCard("Properties", String.valueOf(db.loadAllProperties().size()), "🏠", "#9b59b6"),
            premiumStatCard("System Revenue", String.format("PKR %.0f", PaymentEngine.getInstance().getSystemBalance()), "💰", "#e67e22")
        );

        VBox recentNotifs = new VBox(15);
        recentNotifs.setStyle(StyleManager.card());
        Label notifTitle = new Label("🔔 Recent Activity Alerts");
        notifTitle.setStyle(StyleManager.sectionLabel());
        recentNotifs.getChildren().add(notifTitle);
        
        List<notification.Notification> unread = NotificationCenter.getInstance().getUnread();
        if (unread.isEmpty()) {
            recentNotifs.getChildren().add(new Label("System status: Optimal. No pending alerts."));
        } else {
            for (int i = 0; i < Math.min(3, unread.size()); i++) {
                Label l = new Label("• " + unread.get(i).getMessage());
                l.setStyle("-fx-font-size: 13px; -fx-text-fill: " + StyleManager.COLOR_TEXT + ";");
                recentNotifs.getChildren().add(l);
            }
        }

        setContent(title, cards, recentNotifs);
    }

    // ─── NEW FEATURE: CREATE PROFILE ───
    private void showCreateProfile() {
        Label title = sectionTitle("Create New User Profile");
        
        VBox form = new VBox(20);
        form.setStyle(StyleManager.card());
        form.setMaxWidth(600);

        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("Agent", "Client");
        roleBox.setValue("Agent");
        roleBox.setStyle(StyleManager.comboBox());
        roleBox.setPrefWidth(Double.MAX_VALUE);

        TextField nameF   = styledField("Full Name");
        TextField emailF  = styledField("Email Address");
        TextField phoneF  = styledField("Phone Number");
        TextField cnicF   = styledField("CNIC (XXXXX-XXXXXXX-X)");
        PasswordField passF = new PasswordField();
        passF.setPromptText("Secure Password");
        passF.setStyle(StyleManager.textField());

        VBox agentFields = new VBox(15);
        TextField salaryF = styledField("Monthly Salary (PKR)");
        TextField licF    = styledField("License Number");
        agentFields.getChildren().addAll(fieldLabel("Salary"), salaryF, fieldLabel("License"), licF);

        roleBox.setOnAction(e -> agentFields.setVisible("Agent".equals(roleBox.getValue())));

        Button createBtn = new Button("🚀 Create Profile & Activate");
        createBtn.setStyle(StyleManager.primaryButton());
        createBtn.setPrefWidth(Double.MAX_VALUE);

        createBtn.setOnAction(e -> {
            try {
                String name = nameF.getText().trim();
                String email = emailF.getText().trim().toLowerCase();
                String pass = passF.getText();
                
                if (db.emailExists(email)) {
                    AlertHelper.showError("Error", "Email already exists.");
                    return;
                }

                String hash = PasswordUtils.hashPassword(pass);
                int id = db.getNextPersonId();

                if ("Agent".equals(roleBox.getValue())) {
                    Agent a = new Agent(id, name, email, phoneF.getText(), cnicF.getText(), hash,
                                        licF.getText(), Double.parseDouble(salaryF.getText()), 2.5);
                    a.setAccountStatus(AccountStatus.ACTIVE);
                    db.saveAgent(a);
                } else {
                    Client c = new Client(id, name, email, phoneF.getText(), cnicF.getText(), hash, 0.0);
                    c.setAccountStatus(AccountStatus.ACTIVE);
                    db.saveClient(c);
                }

                AlertHelper.showSuccess("Success", "Profile created and activated successfully.");
                showOverview();
            } catch (Exception ex) {
                AlertHelper.showError("Creation Failed", "Please validate all inputs. " + ex.getMessage());
            }
        });

        form.getChildren().addAll(
            fieldLabel("User Role"), roleBox,
            fieldLabel("Name"), nameF,
            fieldLabel("Email"), emailF,
            fieldLabel("Phone"), phoneF,
            fieldLabel("CNIC"), cnicF,
            fieldLabel("Password"), passF,
            agentFields,
            createBtn
        );

        setContent(title, form);
    }

    // ─── Standard Panels (Redesigned) ───

    private void showRegistrations() {
        Label title = sectionTitle("Pending Registrations");
        VBox list = new VBox(15);
        List<RegistrationRequest> requests = db.loadPendingRequests();
        
        if (requests.isEmpty()) list.getChildren().add(new Label("No pending requests. System is up to date."));
        else {
            for (RegistrationRequest req : requests) {
                HBox card = new HBox(20);
                card.setStyle(StyleManager.card());
                card.setAlignment(Pos.CENTER_LEFT);
                
                VBox info = new VBox(5);
                Label n = new Label(req.getName() + " (" + req.getRequestedRole() + ")");
                n.setStyle(StyleManager.sectionLabel());
                info.getChildren().addAll(n, new Label(req.getEmail()), new Label(req.getPhone()));
                
                Region s = new Region(); HBox.setHgrow(s, Priority.ALWAYS);
                
                Button app = new Button("Approve"); app.setStyle(StyleManager.successButton());
                app.setOnAction(e -> {
                    try {
                        int newId = db.getNextPersonId();
                        if (req.getRequestedRole() == UserRole.CLIENT) {
                            Client c = new Client(newId, req.getName(), req.getEmail(), req.getPhone(), req.getCnic(), req.getPasswordHash(), 0.0);
                            c.setAccountStatus(AccountStatus.ACTIVE);
                            db.saveClient(c);
                        } else {
                            Agent a = new Agent(newId, req.getName(), req.getEmail(), req.getPhone(), req.getCnic(), req.getPasswordHash(), "LIC-" + System.currentTimeMillis() % 10000, 0.0, 2.5);
                            a.setAccountStatus(AccountStatus.ACTIVE);
                            db.saveAgent(a);
                        }
                        db.updateRequestStatus(req.getEmail(), RequestStatus.APPROVED);
                        AlertHelper.showSuccess("Approved", "Profile activated for " + req.getName());
                        showRegistrations();
                    } catch (Exception ex) {
                        AlertHelper.showError("Error", ex.getMessage());
                    }
                });

                Button rej = new Button("Reject"); rej.setStyle(StyleManager.dangerButton());
                rej.setOnAction(e -> {
                    db.updateRequestStatus(req.getEmail(), RequestStatus.REJECTED);
                    AlertHelper.showSuccess("Rejected", "Request from " + req.getName() + " has been declined.");
                    showRegistrations();
                });
                
                card.getChildren().addAll(info, s, app, rej);
                list.getChildren().add(card);
            }
        }
        setContent(title, list);
    }

    private void showProperties() {
        Label title = sectionTitle("Property Catalog Management");
        TableView<Property> table = new TableView<>();
        table.setStyle(StyleManager.tableView());
        table.setPrefHeight(600);
        
        // Simplified columns for brevity in redesign, actual logic remains
        table.getColumns().add(col("ID", Property::getPropertyId, 60));
        table.getColumns().add(col("Title", Property::getTitle, 250));
        table.getColumns().add(col("Type", Property::getPropertyType, 120));
        table.getColumns().add(col("Price", p -> String.format("%.0f", p.calculatePrice()), 150));
        table.getColumns().add(col("Status", p -> p.getStatus().name(), 120));
        
        table.setItems(FXCollections.observableArrayList(db.loadAllProperties()));
        setContent(title, table);
    }

    private void showPayments() {
        Label title = sectionTitle("Financial Ledger");
        VBox list = new VBox(15);
        for (Payment p : PaymentEngine.getInstance().getAllPayments()) {
            HBox card = new HBox(15);
            card.setStyle(StyleManager.card());
            card.setAlignment(Pos.CENTER_LEFT);
            Label l = new Label(String.format("[%s] %s -> %s: PKR %.2f | %s", p.getType(), p.getPayer(), p.getPayee(), p.getAmount(), p.getStatus()));
            if (p.getType() == PaymentType.PURCHASE || p.getType() == PaymentType.SALE || p.getType() == PaymentType.RENT) {
                l.setText(l.getText() + " (Includes Agent Commission)");
            }
            card.getChildren().add(l);
            
            if (p.getStatus() == PaymentStatus.PENDING_ADMIN) {
                Region s = new Region(); HBox.setHgrow(s, Priority.ALWAYS);
                Button app = new Button("Approve"); app.setStyle(StyleManager.successButton());
                app.setOnAction(e -> {
                    p.approve();
                    db.updatePaymentStatus(p.getPaymentId(), PaymentStatus.APPROVED);
                    showPayments();
                });
                Button rej = new Button("Reject"); rej.setStyle(StyleManager.dangerButton());
                rej.setOnAction(e -> {
                    p.reject("Rejected by Admin");
                    db.updatePaymentStatus(p.getPaymentId(), PaymentStatus.REJECTED);
                    showPayments();
                });
                card.getChildren().addAll(s, app, rej);
            }
            list.getChildren().add(card);
        }
        setContent(title, list);
    }

    private void showClients() {
        Label title = sectionTitle("Client Database");
        TableView<Client> table = new TableView<>();
        table.setStyle(StyleManager.tableView());
        table.getColumns().addAll(
            col("ID", Client::getPersonId, 60),
            col("Name", Client::getName, 180),
            col("Email", Client::getEmail, 220),
            col("Status", c -> c.getAccountStatus().name(), 120),
            actionCol("clients", this::showClients)
        );
        table.setItems(FXCollections.observableArrayList(db.loadAllClients()));
        setContent(title, table);
    }

    private void showAgents() {
        Label title = sectionTitle("Agent Roster");
        TableView<Agent> table = new TableView<>();
        table.setStyle(StyleManager.tableView());
        table.getColumns().addAll(
            col("ID", Agent::getPersonId, 60),
            col("Name", Agent::getName, 180),
            col("Salary", Agent::getMonthlySalary, 120),
            col("Status", a -> a.getAccountStatus().name(), 120),
            actionCol("agents", this::showAgents)
        );
        table.setItems(FXCollections.observableArrayList(db.loadAllAgents()));
        setContent(title, table);
    }

    private void showAuditLog() {
        Label title = sectionTitle("System Audit Logs");
        TextArea log = new TextArea(String.join("\n", AuditLog.getInstance().getLogs()));
        log.setEditable(false);
        log.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 12px;");
        log.setPrefHeight(600);
        setContent(title, log);
    }

    private void showNotifications() {
        Label title = sectionTitle("Alert History");
        VBox list = new VBox(10);
        for (notification.Notification n : NotificationCenter.getInstance().getAll()) {
            Label l = new Label("[" + n.getTimestamp() + "] " + n.getMessage());
            l.setStyle(StyleManager.card());
            l.setPrefWidth(Double.MAX_VALUE);
            list.getChildren().add(l);
        }
        setContent(title, list);
    }

    private void showMessages() {
        Label title = sectionTitle("Administrative Communication Hub");
        VBox list = new VBox(10);
        List<Integer> partners = db.loadRecentChatPartners(0);
        if (partners.isEmpty()) list.getChildren().add(new Label("No active administrative chats."));
        for (Integer pid : partners) {
            Person p = db.findById(pid);
            if (p != null) {
                Button b = new Button("Chat with " + p.getName() + " (" + p.getRole() + ")");
                b.setStyle(StyleManager.card() + "-fx-cursor: hand;"); b.setPrefWidth(Double.MAX_VALUE);
                Person finalP = p; b.setOnAction(e -> new ChatWindow(new Admin(), finalP).show());
                list.getChildren().add(b);
            }
        }
        setContent(title, list);
    }

    // ─── HELPERS ───

    private VBox premiumStatCard(String title, String val, String icon, String color) {
        VBox card = new VBox(10);
        card.setPrefSize(220, 120);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 20;");
        card.setEffect(new DropShadow(10, Color.color(0, 0, 0, 0.05)));
        
        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);
        Label i = new Label(icon); i.setStyle("-fx-font-size: 24px; -fx-text-fill: " + color + ";");
        Label t = new Label(title); t.setStyle("-fx-font-size: 14px; -fx-text-fill: " + StyleManager.COLOR_TEXT_LIGHT + ";");
        top.getChildren().addAll(i, t);
        
        Label v = new Label(val);
        v.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + StyleManager.COLOR_TEXT + ";");
        
        card.getChildren().addAll(top, v);
        return card;
    }

    private TextField styledField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle(StyleManager.textField());
        return tf;
    }

    private Label fieldLabel(String text) {
        Label l = new Label(text);
        l.setStyle(StyleManager.fieldLabel());
        return l;
    }

    private Label sectionTitle(String text) {
        Label l = new Label(text);
        l.setStyle(StyleManager.titleLabel());
        return l;
    }

    private <T> TableColumn<T, String> col(String header, java.util.function.Function<T, Object> mapper, double width) {
        TableColumn<T, String> c = new TableColumn<>(header);
        c.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(mapper.apply(d.getValue()))));
        c.setPrefWidth(width);
        return c;
    }

    private <T extends Person> TableColumn<T, String> actionCol(String tableType, Runnable refresh) {
        TableColumn<T, String> col = new TableColumn<>("Management Actions");
        col.setCellFactory(param -> new TableCell<>() {
            private final Button btnBlock = new Button();
            private final Button btnDel   = new Button("Remove");
            private final HBox   box      = new HBox(10, btnBlock, btnDel);

            {
                box.setAlignment(Pos.CENTER);
                btnDel.setStyle(StyleManager.dangerButton() + "-fx-padding: 5 15; -fx-font-size: 11px;");
                btnDel.setOnAction(e -> {
                    T person = getTableView().getItems().get(getIndex());
                    if (AlertHelper.showConfirm("Delete Profile", "Permanently remove " + person.getName() + " from the system?")) {
                        db.deletePerson(person.getPersonId(), tableType);
                        AuditLog.getInstance().logProfileDeleted(person.getEmail(), person.getRole());
                        refresh.run();
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    T person = getTableView().getItems().get(getIndex());
                    boolean isSuspended = person.getAccountStatus() == AccountStatus.SUSPENDED;
                    btnBlock.setText(isSuspended ? "Unblock" : "Block");
                    btnBlock.setStyle((isSuspended ? StyleManager.successButton() : StyleManager.warningButton()) 
                                     + "-fx-padding: 5 15; -fx-font-size: 11px;");
                    btnBlock.setOnAction(e -> {
                        AccountStatus next = isSuspended ? AccountStatus.ACTIVE : AccountStatus.SUSPENDED;
                        db.updateAccountStatus(person.getEmail(), next);
                        AuditLog.getInstance().logAccountStatusChanged(person.getEmail(), next.name());
                        refresh.run();
                    });
                    setGraphic(box);
                }
            }
        });
        col.setPrefWidth(220);
        return col;
    }

    private void handleLogout() {
        Session.getInstance().logout();
        stage.hide();
        new LoginScreen(stage).show();
    }
}
