package gui;

import database.DatabaseManager;
import enums.*;
import model.*;
import notification.*;
import payment.*;
import security.*;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.*;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

import java.io.File;
import java.util.List;

/**
 * Premium Agent Dashboard — Redesigned with Java-based styling.
 * Added: Property Image Upload and Preview.
 */
public class AgentDashboard {

    private final Stage           stage;
    private final Agent           agent;
    private final DatabaseManager db;

    private Button activeBtn;
    private VBox   contentArea;
    private String selectedImagePath;

    public AgentDashboard(Stage stage, Agent agent) {
        this.stage = stage;
        this.agent = agent;
        this.db    = DatabaseManager.getInstance();
    }

    public void show() {
        stage.setTitle("REMS — Agent Portal  |  " + agent.getName());
        stage.setWidth(1280);
        stage.setHeight(800);
        stage.setResizable(true);

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
        Label icon = new Label("💎 REMS");
        icon.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label agentName = new Label(agent.getName().toUpperCase());
        agentName.setStyle("-fx-font-size: 12px; -fx-text-fill: " + StyleManager.COLOR_ACCENT + "; -fx-letter-spacing: 1;");
        brand.getChildren().addAll(icon, agentName);

        VBox navBox = new VBox(2);
        navBox.getChildren().addAll(
            navBtn("📊  Overview",       () -> showOverview()),
            navBtn("🏠  My Listings",    () -> showMyListings()),
            navBtn("➕  Add Property",   () -> showAddProperty()),
            navBtn("🤝  Offers",         () -> showOffers()),
            navBtn("💬  Messages",       () -> showMessages()),
            navBtn("✅  Closed Deals",   () -> showClosedDeals()),
            navBtn("💵  My Earnings",    () -> showEarnings()),
            navBtn("👤  My Profile",     () -> showProfile())
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
        btn.setOnAction(e -> { setActiveBtn(btn); action.run(); });
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

    private void showOverview() {
        Label title = sectionTitle("Performance Overview");
        
        FlowPane cards = new FlowPane(20, 20);
        cards.getChildren().addAll(
            agentStatCard("Wallet Balance", String.format("PKR %.0f", agent.getWalletBalance()), "💰", "#3498db"),
            agentStatCard("Managed Listings", String.valueOf(agent.getManagedProperties().size()), "🏠", "#2ecc71"),
            agentStatCard("Closed Deals", String.valueOf(agent.getClosedDeals().size()), "✅", "#9b59b6"),
            agentStatCard("Total Earnings", String.format("PKR %.0f", agent.getTotalEarnings()), "📈", "#e67e22")
        );

        Label recentTitle = sectionTitle("Recent Property Listings");
        FlowPane recentProps = new FlowPane(15, 15);
        List<Property> props = agent.getManagedProperties();
        if (props.isEmpty()) recentProps.getChildren().add(new Label("No properties listed."));
        else {
            for (int i = 0; i < Math.min(4, props.size()); i++)
                recentProps.getChildren().add(buildPremiumPropertyCard(props.get(i)));
        }

        setContent(title, cards, recentTitle, recentProps);
    }

    private void showMyListings() {
        Label title = sectionTitle("My Active Portfolio");
        TableView<Property> table = new TableView<>();
        table.setStyle(StyleManager.tableView());
        table.setPrefHeight(600);
        
        table.getColumns().add(tcol("ID", Property::getPropertyId, 60));
        table.getColumns().add(tcol("Title", Property::getTitle, 250));
        table.getColumns().add(tcol("Status", p -> p.getStatus().name(), 120));
        table.getColumns().add(tcol("Price", p -> String.format("%.0f", p.calculatePrice()), 150));
        
        table.setItems(FXCollections.observableArrayList(agent.getManagedProperties()));
        setContent(title, table);
    }

    // ─── NEW FEATURE: IMAGE UPLOAD ───
    private void showAddProperty() {
        Label title = sectionTitle("Submit New Property Listing");
        selectedImagePath = null;
        
        VBox form = new VBox(20);
        form.setStyle(StyleManager.card());
        form.setMaxWidth(800);

        HBox split = new HBox(30);
        VBox left = new VBox(15);
        left.setPrefWidth(400);
        VBox right = new VBox(15);
        right.setAlignment(Pos.TOP_CENTER);
        
        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("Apartment", "House", "Plot", "Office", "Shop");
        typeBox.setValue("Apartment");
        typeBox.setStyle(StyleManager.comboBox());
        typeBox.setPrefWidth(Double.MAX_VALUE);

        ComboBox<PropertyMode> modeBox = new ComboBox<>();
        modeBox.getItems().addAll(PropertyMode.FOR_SALE, PropertyMode.FOR_RENT);
        modeBox.setValue(PropertyMode.FOR_SALE);
        modeBox.setStyle(StyleManager.comboBox());
        modeBox.setPrefWidth(Double.MAX_VALUE);

        TextField titleF = styledTf("Property Title");
        TextField addrF  = styledTf("Full Address");
        TextField cityF  = styledTf("City");
        TextField areaF  = styledTf("Area (sq ft)");
        TextField priceF = styledTf("Asking Price (PKR)");

        left.getChildren().addAll(
            fieldLabel("Property Type"), typeBox,
            fieldLabel("Listing Mode"), modeBox,
            fieldLabel("Title"), titleF,
            fieldLabel("Address"), addrF,
            fieldLabel("City"), cityF,
            fieldLabel("Area"), areaF,
            fieldLabel("Price"), priceF
        );

        // Image Upload Section
        Label imgLabel = new Label("Property Visuals");
        imgLabel.setStyle(StyleManager.sectionLabel());
        
        ImageView preview = new ImageView();
        preview.setFitWidth(250);
        preview.setFitHeight(180);
        preview.setPreserveRatio(true);
        preview.setStyle("-fx-border-color: #ddd; -fx-border-width: 1;");
        
        Button uploadBtn = new Button("🖼️ Select Property Image");
        uploadBtn.setStyle(StyleManager.secondaryButton());
        uploadBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
            File file = fc.showOpenDialog(stage);
            if (file != null) {
                selectedImagePath = file.getAbsolutePath();
                preview.setImage(new Image("file:" + selectedImagePath));
            }
        });

        right.getChildren().addAll(imgLabel, preview, uploadBtn);
        split.getChildren().addAll(left, right);

        Button submitBtn = new Button("📤 Submit Listing for Review");
        submitBtn.setStyle(StyleManager.primaryButton());
        submitBtn.setPrefWidth(Double.MAX_VALUE);
        submitBtn.setOnAction(e -> {
            try {
                int id = db.getNextPropertyId();
                Property p = switch (typeBox.getValue()) {
                    case "House"     -> new House(id, titleF.getText(), addrF.getText(), cityF.getText(), Double.parseDouble(areaF.getText()), Double.parseDouble(priceF.getText()), modeBox.getValue(), 3, 2, true, true, 500, 2);
                    case "Plot"      -> new Plot(id, titleF.getText(), addrF.getText(), cityF.getText(), Double.parseDouble(areaF.getText()), Double.parseDouble(priceF.getText()), modeBox.getValue(), "Residential", false, true, "North");
                    case "Office"    -> new Office(id, titleF.getText(), addrF.getText(), cityF.getText(), Double.parseDouble(areaF.getText()), Double.parseDouble(priceF.getText()), modeBox.getValue(), "Business", 10, 20, true);
                    case "Shop"      -> new Shop(id, titleF.getText(), addrF.getText(), cityF.getText(), Double.parseDouble(areaF.getText()), Double.parseDouble(priceF.getText()), modeBox.getValue(), "Commercial", 5, "Retail", true);
                    default          -> new Apartment(id, titleF.getText(), addrF.getText(), cityF.getText(), Double.parseDouble(areaF.getText()), Double.parseDouble(priceF.getText()), modeBox.getValue(), 2, 2, true, 1, true, true);
                };
                p.setImagePath(selectedImagePath);
                p.setAssignedAgent(agent);
                agent.addProperty(p);
                db.saveProperty(p);
                
                // Notify Admin
                NotificationCenter.getInstance().push(new notification.Notification(
                    NotificationType.SYSTEM, 
                    "New Property Uploaded: " + p.getTitle() + " by Agent " + agent.getName(),
                    agent.getEmail()
                ));

                AlertHelper.showSuccess("Success", "Listing published successfully.");
                showMyListings();
            } catch (Exception ex) {
                AlertHelper.showError("Submission Failed", "Check numeric fields. " + ex.getMessage());
            }
        });

        form.getChildren().addAll(split, new Separator(), submitBtn);
        setContent(title, form);
    }

    private void showClosedDeals() {
        Label title = sectionTitle("Successfully Closed Deals");
        TableView<Transaction> table = new TableView<>();
        table.setStyle(StyleManager.tableView());
        table.getColumns().addAll(tcol("ID", Transaction::getTransactionId, 60), tcol("Property", t -> t.getProperty().getTitle(), 250), tcol("Commission", Transaction::calculateAgentCommission, 150));
        table.setItems(FXCollections.observableArrayList(agent.getClosedDeals()));
        setContent(title, table);
    }

    private void showEarnings() {
        Label title = sectionTitle("My Financial Earnings");
        VBox card = new VBox(15);
        card.setStyle(StyleManager.card());
        card.getChildren().addAll(new Label("Current Balance: PKR " + agent.getWalletBalance()), new Label("Total Career Earnings: PKR " + agent.getTotalEarnings()));
        setContent(title, card);
    }

    private void showProfile() {
        Label title = sectionTitle("My Profile & Security");
        VBox form = new VBox(15);
        form.setStyle(StyleManager.card());
        form.getChildren().addAll(fieldLabel("Name"), styledTf(agent.getName()), fieldLabel("Phone"), styledTf(agent.getPhone()));
        setContent(title, form);
    }

    private void showOffers() {
        Label title = sectionTitle("Property Price Negotiations");
        TableView<Offer> table = new TableView<>();
        table.setStyle(StyleManager.tableView());
        table.getColumns().add(tcol("Date", Offer::getTimestamp, 150));
        table.getColumns().add(tcol("Amount", Offer::getAmount, 120));
        table.getColumns().add(tcol("Status", Offer::getStatus, 120));
        
        TableColumn<Offer, String> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(p -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    Offer o = getTableView().getItems().get(getIndex());
                    if (o.getStatus() == OfferStatus.PENDING) {
                        Button acc = new Button("Accept"); acc.setStyle(StyleManager.successButton());
                        acc.setOnAction(e -> { db.updateOfferStatus(o.getOfferId(), OfferStatus.ACCEPTED); showOffers(); });
                        Button rej = new Button("Reject"); rej.setStyle(StyleManager.dangerButton());
                        rej.setOnAction(e -> { db.updateOfferStatus(o.getOfferId(), OfferStatus.REJECTED); showOffers(); });
                        setGraphic(new HBox(10, acc, rej));
                    } else setGraphic(new Label(o.getStatus().name()));
                }
            }
        });
        table.getColumns().add(actionCol);
        table.setItems(FXCollections.observableArrayList(db.loadOffersForUser(agent.getPersonId())));
        setContent(title, table);
    }

    private void showMessages() {
        Label title = sectionTitle("Recent Communications");
        VBox list = new VBox(10);
        List<Integer> partners = db.loadRecentChatPartners(agent.getPersonId());
        if (partners.isEmpty()) list.getChildren().add(new Label("No recent messages."));
        for (Integer pid : partners) {
            Person p = db.loadAllClients().stream().filter(c -> c.getPersonId() == pid).findFirst().orElse(null);
            if (p == null) p = db.loadAllAgents().stream().filter(a -> a.getPersonId() == pid).findFirst().orElse(null);
            if (p != null) {
                Button b = new Button("Chat with " + p.getName() + " (" + p.getEmail() + ")");
                b.setStyle(StyleManager.card() + "-fx-cursor: hand;"); b.setPrefWidth(Double.MAX_VALUE);
                Person finalP = p; b.setOnAction(e -> new ChatWindow(agent, finalP).show());
                list.getChildren().add(b);
            }
        }
        setContent(title, list);
    }

    // ─── HELPERS ───

    private VBox buildPremiumPropertyCard(Property p) {
        VBox card = new VBox(10);
        card.setPrefWidth(260);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 0;");
        card.setEffect(new DropShadow(10, Color.color(0, 0, 0, 0.05)));
        card.setCursor(javafx.scene.Cursor.HAND);
        
        card.setOnMouseClicked(e -> new PropertyDetailsWindow(p).show());

        ImageView iv = new ImageView();
        iv.setFitWidth(260);
        iv.setFitHeight(150);
        if (p.getImagePath() != null && new File(p.getImagePath()).exists()) {
            iv.setImage(new Image("file:" + p.getImagePath()));
        } else {
            // Placeholder logic if needed
        }
        
        VBox info = new VBox(5);
        info.setPadding(new Insets(15));
        Label t = new Label(p.getTitle()); t.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label pr = new Label("PKR " + String.format("%.0f", p.calculatePrice())); pr.setStyle("-fx-text-fill: " + StyleManager.COLOR_SUCCESS + "; -fx-font-weight: bold;");
        info.getChildren().addAll(t, pr);
        
        card.getChildren().addAll(iv, info);
        return card;
    }

    private VBox agentStatCard(String title, String val, String icon, String color) {
        VBox card = new VBox(8);
        card.setPrefSize(240, 110);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 20;");
        card.setEffect(new DropShadow(10, Color.color(0, 0, 0, 0.04)));
        Label i = new Label(icon); i.setStyle("-fx-font-size: 22px; -fx-text-fill: " + color + ";");
        Label t = new Label(title); t.setStyle("-fx-font-size: 13px; -fx-text-fill: " + StyleManager.COLOR_TEXT_LIGHT + ";");
        Label v = new Label(val); v.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        card.getChildren().addAll(new HBox(10, i, t), v);
        return card;
    }

    private TextField styledTf(String prompt) {
        TextField tf = new TextField(); tf.setPromptText(prompt); tf.setStyle(StyleManager.textField()); return tf;
    }

    private Label fieldLabel(String t) {
        Label l = new Label(t); l.setStyle(StyleManager.fieldLabel()); return l;
    }

    private Label sectionTitle(String t) {
        Label l = new Label(t); l.setStyle(StyleManager.titleLabel()); return l;
    }

    private <T> TableColumn<T, String> tcol(String h, java.util.function.Function<T, Object> m, double w) {
        TableColumn<T, String> c = new TableColumn<>(h);
        c.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(m.apply(d.getValue()))));
        c.setPrefWidth(w);
        return c;
    }

    private void handleLogout() {
        Session.getInstance().logout();
        stage.hide();
        new LoginScreen(stage).show();
    }
}
