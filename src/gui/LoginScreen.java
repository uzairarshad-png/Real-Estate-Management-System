package gui;

import database.DatabaseManager;
import enums.AccountStatus;
import enums.NotificationType;
import enums.RegistrationSource;
import enums.UserRole;
import exceptions.*;
import model.*;
import notification.Notification;
import notification.NotificationCenter;
import notification.PasswordResetRequest;
import notification.RegistrationRequest;
import security.*;

import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

/**
 * Redesigned Login Screen — Premium Java-based styling.
 */
public class LoginScreen {

    private final Stage stage;
    private final DatabaseManager db;
    private final LoginAttemptTracker tracker;

    public LoginScreen(Stage stage) {
        this.stage   = stage;
        this.db      = DatabaseManager.getInstance();
        this.tracker = new LoginAttemptTracker();
    }

    public void show() {
        stage.setTitle("REMS — Premium Real Estate Portal");
        stage.setWidth(1000);
        stage.setHeight(720);
        stage.setResizable(true);

        BorderPane root = new BorderPane();
        root.setLeft(buildBrandPanel());
        root.setCenter(buildFormPanel());
        root.setStyle("-fx-background-color: " + StyleManager.COLOR_BG + ";");

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.centerOnScreen();
        
        // Simple entry animation
        FadeTransition ft = new FadeTransition(Duration.millis(800), root);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();

        stage.show();
    }

    private VBox buildBrandPanel() {
        VBox panel = new VBox(20);
        panel.setAlignment(Pos.CENTER);
        panel.setPrefWidth(380);
        panel.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, "
            + StyleManager.COLOR_PRIMARY + ", "
            + StyleManager.COLOR_PRIMARY_DARK + "); "
            + "-fx-padding: 60;"
        );
        VBox.setVgrow(panel, Priority.ALWAYS);

        Label icon = new Label("💎");
        icon.setStyle("-fx-font-size: 80px;");

        Label appName = new Label("REMS");
        appName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 42));
        appName.setTextFill(Color.WHITE);

        Label subtitle = new Label("ELEGANCE IN REAL ESTATE");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: rgba(255,255,255,0.7); -fx-letter-spacing: 2;");

        Region spacer = new Region();
        spacer.setPrefHeight(40);

        VBox feats = new VBox(15);
        feats.setAlignment(Pos.CENTER_LEFT);
        feats.getChildren().addAll(
            featRow("🛡️", "Secure SHA-256 Authentication"),
            featRow("🏠", "Verified Property Listings"),
            featRow("📊", "Advanced Admin Analytics"),
            featRow("💳", "Instant Payment Processing")
        );

        panel.getChildren().addAll(icon, appName, subtitle, spacer, feats);
        return panel;
    }

    private HBox featRow(String icon, String text) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        Label i = new Label(icon);
        i.setStyle("-fx-font-size: 16px;");
        Label t = new Label(text);
        t.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.9);");
        row.getChildren().addAll(i, t);
        return row;
    }

    private VBox buildFormPanel() {
        VBox container = new VBox();
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(40));
        container.setStyle("-fx-background-color: " + StyleManager.COLOR_BG + ";");

        VBox formCard = new VBox(25);
        formCard.setStyle(StyleManager.card());
        formCard.setMaxWidth(480);
        formCard.setAlignment(Pos.TOP_CENTER);

        // Custom Segmented Toggle for Login/SignUp
        HBox toggleContainer = new HBox(0);
        toggleContainer.setAlignment(Pos.CENTER);
        toggleContainer.setStyle("-fx-background-color: #eee; -fx-background-radius: 10; -fx-padding: 4;");
        toggleContainer.setMaxWidth(300);

        Button loginTab = new Button("Login");
        Button signupTab = new Button("Create Account");
        
        String activeTab = "-fx-background-color: white; -fx-text-fill: " + StyleManager.COLOR_PRIMARY + "; -fx-font-weight: bold; -fx-background-radius: 8; -fx-pref-width: 145; -fx-pref-height: 35; -fx-cursor: hand;";
        String inactiveTab = "-fx-background-color: transparent; -fx-text-fill: #777; -fx-font-weight: normal; -fx-pref-width: 145; -fx-pref-height: 35; -fx-cursor: hand;";

        StackPane contentStack = new StackPane();
        VBox loginView = buildLoginFormView();
        VBox signupView = buildSignUpFormView();
        signupView.setVisible(false);
        contentStack.getChildren().addAll(loginView, signupView);

        loginTab.setStyle(activeTab);
        signupTab.setStyle(inactiveTab);

        loginTab.setOnAction(e -> {
            loginTab.setStyle(activeTab);
            signupTab.setStyle(inactiveTab);
            loginView.setVisible(true);
            signupView.setVisible(false);
        });

        signupTab.setOnAction(e -> {
            signupTab.setStyle(activeTab);
            loginTab.setStyle(inactiveTab);
            loginView.setVisible(false);
            signupView.setVisible(true);
        });

        toggleContainer.getChildren().addAll(loginTab, signupTab);

        formCard.getChildren().addAll(toggleContainer, contentStack);
        
        ScrollPane sp = new ScrollPane(formCard);
        sp.setFitToWidth(true);
        sp.setStyle(StyleManager.scrollPane() + "-fx-background-color: transparent;");
        sp.setMaxHeight(650); // Prevent infinite growth
        
        container.getChildren().add(sp);
        return container;
    }

    private VBox buildLoginFormView() {
        VBox form = new VBox(18);
        form.setAlignment(Pos.TOP_CENTER);
        form.setStyle("-fx-background-color: white;");

        Label title = new Label("Welcome Back");
        title.setStyle(StyleManager.titleLabel());

        HBox roleToggle = new HBox(10);
        roleToggle.setAlignment(Pos.CENTER);
        ToggleGroup group = new ToggleGroup();
        ToggleButton rb1 = roleBtn("Admin", group);
        ToggleButton rb2 = roleBtn("Client", group);
        ToggleButton rb3 = roleBtn("Agent", group);
        rb2.setSelected(true);
        roleToggle.getChildren().addAll(rb1, rb2, rb3);

        VBox inputs = new VBox(10);
        inputs.getChildren().addAll(
            fieldLabel("Email Address"),
            styledTextField("e.g. admin@rems.com"),
            errorLabel(),
            fieldLabel("Password"),
            styledPasswordField("••••••••"),
            errorLabel()
        );

        Button loginBtn = new Button("Sign In to Portal");
        loginBtn.setStyle(StyleManager.primaryButton());
        loginBtn.setPrefWidth(Double.MAX_VALUE);
        loginBtn.setPrefHeight(50);
        applyHoverEffect(loginBtn, StyleManager.primaryButton(), StyleManager.primaryButtonHover());

        Button forgotBtn = new Button("Forgot password or locked out?");
        forgotBtn.setStyle(StyleManager.linkButton());

        Label statusMsg = new Label("");
        statusMsg.setWrapText(true);
        statusMsg.setMaxWidth(340);

        loginBtn.setOnAction(e -> {
            TextField emailField = (TextField) inputs.getChildren().get(1);
            PasswordField passField = (PasswordField) inputs.getChildren().get(4);
            Label emailErr = (Label) inputs.getChildren().get(2);
            Label passErr = (Label) inputs.getChildren().get(5);
            String selectedRole = (String) group.getSelectedToggle().getUserData();
            handleLogin(emailField, passField, emailErr, passErr, new Label(), statusMsg, selectedRole);
        });

        form.getChildren().addAll(title, roleToggle, new Separator(), inputs, loginBtn, forgotBtn, statusMsg);
        return form;
    }

    private VBox buildSignUpFormView() {
        VBox form = new VBox(15);
        form.setAlignment(Pos.TOP_CENTER);
        form.setStyle("-fx-background-color: white;");

        Label title = new Label("Join REMS");
        title.setStyle(StyleManager.titleLabel());

        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("Client", "Agent");
        roleBox.setValue("Client");
        roleBox.setStyle(StyleManager.comboBox());
        roleBox.setPrefWidth(Double.MAX_VALUE);

        VBox inputs = new VBox(8);
        inputs.getChildren().addAll(
            fieldLabel("Select Role"), roleBox,
            fieldLabel("Full Name"), styledTextField("John Doe"),
            fieldLabel("Email Address"), styledTextField("john@example.com"),
            fieldLabel("Phone Number"), styledTextField("0300-1234567"),
            fieldLabel("CNIC"), styledTextField("35201-1234567-9"),
            fieldLabel("Password"), styledPasswordField("••••••••"),
            fieldLabel("Confirm Password"), styledPasswordField("••••••••")
        );

        Button submitBtn = new Button("Create Account");
        submitBtn.setStyle(StyleManager.accentButton());
        submitBtn.setPrefWidth(Double.MAX_VALUE);
        submitBtn.setPrefHeight(50);

        Label statusMsg = new Label("");
        statusMsg.setWrapText(true);
        statusMsg.setMaxWidth(340);

        submitBtn.setOnAction(e -> {
            String role   = roleBox.getValue();
            String name   = ((TextField) inputs.getChildren().get(3)).getText().trim();
            String email  = ((TextField) inputs.getChildren().get(5)).getText().trim().toLowerCase();
            String phone  = ((TextField) inputs.getChildren().get(7)).getText().trim();
            String cnic   = ((TextField) inputs.getChildren().get(9)).getText().trim();
            String pass   = ((PasswordField) inputs.getChildren().get(11)).getText();
            String conf   = ((PasswordField) inputs.getChildren().get(13)).getText();

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || cnic.isEmpty() || pass.isEmpty()) {
                statusMsg.setText("❌ All fields are required.");
                statusMsg.setStyle(StyleManager.errorLabel());
                return;
            }
            if (!pass.equals(conf)) {
                statusMsg.setText("❌ Passwords do not match.");
                statusMsg.setStyle(StyleManager.errorLabel());
                return;
            }

            try {
                if (db.emailExists(email)) {
                    statusMsg.setText("❌ Email already registered.");
                    statusMsg.setStyle(StyleManager.errorLabel());
                    return;
                }

                String hash = PasswordUtils.hashPassword(pass);
                UserRole userRole = "Agent".equals(role) ? UserRole.AGENT : UserRole.CLIENT;
                
                RegistrationRequest req = new RegistrationRequest(name, email, phone, cnic, hash, userRole, RegistrationSource.LOGIN_PAGE);
                db.saveRegistrationRequest(req);
                
                NotificationCenter.getInstance().push(new Notification(
                    NotificationType.REG_REQUEST, 
                    "New Registration Request: " + name + " (" + role + ")", 
                    email
                ));

                statusMsg.setText("✅ Success! Admin approval pending.");
                statusMsg.setStyle(StyleManager.successLabel());
                
            } catch (Exception ex) {
                statusMsg.setText("❌ Failed: " + ex.getMessage());
                statusMsg.setStyle(StyleManager.errorLabel());
            }
        });

        form.getChildren().addAll(title, new Separator(), inputs, submitBtn, statusMsg);
        return form;
    }

    // ─── Re-implementing handleLogin and others with same logic but better UI feedback ───

    private void handleLogin(TextField emailField, PasswordField passField,
                              Label emailErr, Label passErr,
                              Label attemptsInfo, Label statusMsg, String selectedRole) {
        String email    = emailField.getText().trim().toLowerCase();
        String password = passField.getText();
        boolean valid   = true;

        if (!InputValidator.isValidEmail(email)) {
            emailErr.setText("Please enter a valid email.");
            emailField.setStyle(StyleManager.textFieldError());
            valid = false;
        } else {
            emailErr.setText("");
            emailField.setStyle(StyleManager.textField());
        }

        if (password.isEmpty()) {
            passErr.setText("Password is required.");
            passField.setStyle(StyleManager.textFieldError());
            valid = false;
        } else {
            passErr.setText("");
            passField.setStyle(StyleManager.textField());
        }

        if (!valid) return;

        try {
            if (email.equalsIgnoreCase(Admin.ADMIN_USERNAME)) {
                if (!"Admin".equals(selectedRole)) {
                    statusMsg.setText("❌ Please select 'Admin' role to login as administrator.");
                    statusMsg.setStyle(StyleManager.errorLabel());
                    return;
                }
                if (!PasswordUtils.verifyPassword(password, Admin.ADMIN_PASS_HASH)) {
                    statusMsg.setText("❌ Invalid Admin credentials.");
                    statusMsg.setStyle(StyleManager.errorLabel());
                    return;
                }
                Session.getInstance().login(new Admin());
                AuditLog.getInstance().logSuccess(Admin.ADMIN_USERNAME, "Administrator");
                openAdminDashboard();
                return;
            }

            Person user = db.findByEmail(email);
            if (user == null) throw new UnknownUserIdException(email);
            
            if (user instanceof Client && !"Client".equals(selectedRole)) {
                statusMsg.setText("❌ This account is registered as a Client. Please select 'Client' role.");
                statusMsg.setStyle(StyleManager.errorLabel());
                return;
            }
            if (user instanceof Agent && !"Agent".equals(selectedRole)) {
                statusMsg.setText("❌ This account is registered as an Agent. Please select 'Agent' role.");
                statusMsg.setStyle(StyleManager.errorLabel());
                return;
            }

            if (!PasswordUtils.verifyPassword(password, user.getPasswordHash())) {
                AuditLog.getInstance().logFailure(email, 0); // Tracker not integrated here yet
                statusMsg.setText("❌ Invalid credentials.");
                statusMsg.setStyle(StyleManager.errorLabel());
                return;
            }

            Session.getInstance().login(user);
            AuditLog.getInstance().logSuccess(email, user.getUserRole().name());
            if (user instanceof Client) openClientDashboard((Client) user);
            else if (user instanceof Agent) openAgentDashboard((Agent) user);

        } catch (Exception ex) {
            statusMsg.setText("❌ Error: " + ex.getMessage());
            statusMsg.setStyle(StyleManager.errorLabel());
        }
    }

    // ─── Helpers ───

    private void applyHoverEffect(Button btn, String baseStyle, String hoverStyle) {
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(baseStyle));
    }

    private TextField styledTextField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle(StyleManager.textField());
        tf.focusedProperty().addListener((obs, oldV, newV) -> {
            if (newV) tf.setStyle(StyleManager.textFieldFocused());
            else tf.setStyle(StyleManager.textField());
        });
        return tf;
    }

    private PasswordField styledPasswordField(String prompt) {
        PasswordField pf = new PasswordField();
        pf.setPromptText(prompt);
        pf.setStyle(StyleManager.textField());
        pf.focusedProperty().addListener((obs, oldV, newV) -> {
            if (newV) pf.setStyle(StyleManager.textFieldFocused());
            else pf.setStyle(StyleManager.textField());
        });
        return pf;
    }

    private Label fieldLabel(String text) {
        Label l = new Label(text);
        l.setStyle(StyleManager.fieldLabel());
        return l;
    }

    private Label errorLabel() {
        Label l = new Label("");
        l.setStyle(StyleManager.errorLabel());
        return l;
    }

    private ToggleButton roleBtn(String text, ToggleGroup group) {
        ToggleButton b = new ToggleButton(text);
        b.setToggleGroup(group);
        b.setUserData(text);
        b.setPrefWidth(100);
        
        String base = StyleManager.secondaryButton();
        String active = "-fx-background-color: " + StyleManager.COLOR_PRIMARY + "; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 10 24; -fx-font-weight: bold; -fx-cursor: hand;";
        
        b.setStyle(base);
        b.selectedProperty().addListener((obs, old, isSelected) -> {
            b.setStyle(isSelected ? active : base);
        });
        
        return b;
    }

    private void openAdminDashboard() { stage.hide(); new AdminDashboard(stage).show(); }
    private void openClientDashboard(Client c) { stage.hide(); new ClientDashboard(stage, c).show(); }
    private void openAgentDashboard(Agent a) { stage.hide(); new AgentDashboard(stage, a).show(); }
}
