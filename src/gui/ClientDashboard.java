package gui;

import database.DatabaseManager;
import enums.*;
import exceptions.*;
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
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Premium Client Dashboard — Redesigned with Java-based styling.
 */
public class ClientDashboard {

    private final Stage           stage;
    private final Client          client;
    private final DatabaseManager db;

    private Button activeBtn;
    private VBox   contentArea;

    public ClientDashboard(Stage stage, Client client) {
        this.stage  = stage;
        this.client = client;
        this.db     = DatabaseManager.getInstance();
    }

    public void show() {
        stage.setTitle("REMS — Client Experience  |  " + client.getName());
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
        Label clientName = new Label(client.getName().toUpperCase());
        clientName.setStyle("-fx-font-size: 11px; -fx-text-fill: " + StyleManager.COLOR_ACCENT + "; -fx-letter-spacing: 1.5;");
        brand.getChildren().addAll(icon, clientName);

        VBox navBox = new VBox(2);
        navBox.getChildren().addAll(
            navBtn("📊  Overview",        () -> showOverview()),
            navBtn("🔍  Browse Properties",() -> showBrowse()),
            navBtn("🏠  My Assets",       () -> showMyProperties()),
            navBtn("📋  Ledger",          () -> showTransactions()),
            navBtn("💰  Financials",      () -> showPayments()),
            navBtn("➕  List Property",   () -> showListProperty()),
            navBtn("🤝  My Offers",       () -> showOffers()),
            navBtn("💬  Messages",       () -> showMessages()),
            navBtn("👤  Settings",        () -> showProfile())
        );

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = new Button("🚪  Logout");
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
        Label title = sectionTitle("Your Real Estate Overview");
        
        FlowPane cards = new FlowPane(20, 20);
        cards.getChildren().addAll(
            clientStatCard("Wallet Balance", String.format("PKR %.0f", client.getWalletBalance()), "💰", "#3498db"),
            clientStatCard("Owned Properties", String.valueOf(client.getOwnedProperties().size()), "🏠", "#2ecc71"),
            clientStatCard("Active Rentals", String.valueOf(client.getRentedProperties().size()), "🔑", "#9b59b6"),
            clientStatCard("Total Budget", String.format("PKR %.0f", client.getBudget()), "📊", "#e67e22")
        );

        Label discoverTitle = sectionTitle("Discover New Opportunities");
        FlowPane propCards = new FlowPane(15, 15);
        List<Property> available = db.loadAllProperties().stream()
            .filter(p -> p.getStatus() == PropertyStatus.AVAILABLE || p.getStatus() == PropertyStatus.SOLD)
            .limit(4).collect(Collectors.toList());
            
        if (available.isEmpty()) propCards.getChildren().add(new Label("No properties available right now."));
        else {
            for (Property p : available)
                propCards.getChildren().add(buildPremiumPropertyCard(p));
        }

        setContent(title, cards, discoverTitle, propCards);
    }

    private void showBrowse() {
        Label title = sectionTitle("Explore Premium Properties");
        
        HBox filterBar = new HBox(15);
        filterBar.setStyle(StyleManager.card());
        filterBar.setAlignment(Pos.CENTER_LEFT);
        
        TextField search = styledField("Search location...");
        ComboBox<String> type = new ComboBox<>();
        type.getItems().addAll("All Types", "Apartment", "House", "Plot", "Office", "Shop");
        type.setValue("All Types");
        type.setStyle(StyleManager.comboBox());
        
        Button searchBtn = new Button("Search");
        searchBtn.setStyle(StyleManager.primaryButton());
        
        filterBar.getChildren().addAll(search, type, searchBtn);
        
        FlowPane results = new FlowPane(15, 15);
        loadResults(results);
        
        setContent(title, filterBar, results);
    }

    private void loadResults(FlowPane pane) {
        pane.getChildren().clear();
        for (Property p : db.loadAllProperties()) {
            if (p.getStatus() == PropertyStatus.AVAILABLE || p.getStatus() == PropertyStatus.SOLD) {
                pane.getChildren().add(buildPremiumPropertyCard(p));
            }
        }
    }

    private void showMyProperties() {
        Label title = sectionTitle("Portfolio Assets");
        VBox list = new VBox(15);
        for (Property p : client.getOwnedProperties()) {
            list.getChildren().add(buildPremiumPropertyCard(p));
        }
        setContent(title, list);
    }

    private void showTransactions() {
        Label title = sectionTitle("Transaction Ledger");
        TableView<Transaction> table = new TableView<>();
        table.setStyle(StyleManager.tableView());
        table.getColumns().add(tcol("ID", Transaction::getTransactionId, 60));
        table.getColumns().add(tcol("Property", t -> t.getProperty().getTitle(), 300));
        table.getColumns().add(tcol("Amount", Transaction::getAmount, 150));
        table.setItems(FXCollections.observableArrayList(client.getTransactions()));
        setContent(title, table);
    }

    private void showPayments() {
        Label title = sectionTitle("Financial Management");
        VBox wallet = new VBox(20);
        wallet.setStyle(StyleManager.card());
        wallet.getChildren().addAll(new Label("Current Balance: PKR " + client.getWalletBalance()), new Button("Top Up Wallet"));
        setContent(title, wallet);
    }

    private void showProfile() {
        Label title = sectionTitle("Profile Settings");
        VBox form = new VBox(15);
        form.setStyle(StyleManager.card());
        form.getChildren().addAll(fieldLabel("Name"), styledField(client.getName()), fieldLabel("Phone"), styledField(client.getPhone()));
        setContent(title, form);
    }

    private String selectedImagePath;
    private void showListProperty() {
        Label title = sectionTitle("List Your Property for Sale/Rent");
        selectedImagePath = null;
        VBox form = new VBox(20);
        form.setStyle(StyleManager.card());
        form.setMaxWidth(800);

        HBox split = new HBox(30);
        VBox left = new VBox(15); left.setPrefWidth(400);
        VBox right = new VBox(15); right.setAlignment(Pos.TOP_CENTER);

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("Apartment", "House", "Plot", "Office", "Shop");
        typeBox.setValue("Apartment"); typeBox.setStyle(StyleManager.comboBox()); typeBox.setPrefWidth(Double.MAX_VALUE);

        ComboBox<PropertyMode> modeBox = new ComboBox<>();
        modeBox.getItems().addAll(PropertyMode.FOR_SALE, PropertyMode.FOR_RENT_OUT);
        modeBox.setValue(PropertyMode.FOR_SALE); modeBox.setStyle(StyleManager.comboBox()); modeBox.setPrefWidth(Double.MAX_VALUE);

        TextField titleF = styledField("Property Title");
        TextField addrF  = styledField("Full Address");
        TextField cityF  = styledField("City");
        TextField areaF  = styledField("Area (sq ft)");
        TextField priceF = styledField("Asking Price (PKR)");

        ComboBox<Agent> agentBox = new ComboBox<>();
        agentBox.getItems().addAll(db.loadAllAgents());
        agentBox.setPromptText("Select Agent (Optional)");
        agentBox.setStyle(StyleManager.comboBox()); agentBox.setPrefWidth(Double.MAX_VALUE);

        left.getChildren().addAll(fieldLabel("Type"), typeBox, fieldLabel("Mode"), modeBox, fieldLabel("Title"), titleF, fieldLabel("Address"), addrF, fieldLabel("City"), cityF, fieldLabel("Area"), areaF, fieldLabel("Price"), priceF, fieldLabel("Assign Agent"), agentBox);

        ImageView preview = new ImageView(); preview.setFitWidth(250); preview.setFitHeight(180); preview.setPreserveRatio(true); preview.setStyle("-fx-border-color: #ddd;");
        Button uploadBtn = new Button("🖼️ Select Image"); uploadBtn.setStyle(StyleManager.secondaryButton());
        uploadBtn.setOnAction(e -> {
            javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
            File file = fc.showOpenDialog(stage);
            if (file != null) { selectedImagePath = file.getAbsolutePath(); preview.setImage(new Image("file:" + selectedImagePath)); }
        });
        right.getChildren().addAll(new Label("Property Visuals"), preview, uploadBtn);
        split.getChildren().addAll(left, right);

        Button submitBtn = new Button("🚀 Publish Listing");
        submitBtn.setStyle(StyleManager.primaryButton()); submitBtn.setPrefWidth(Double.MAX_VALUE);
        submitBtn.setOnAction(e -> {
            try {
                int id = db.getNextPropertyId();
                Property p = switch (typeBox.getValue()) {
                    case "House"  -> new House(id, titleF.getText(), addrF.getText(), cityF.getText(), Double.parseDouble(areaF.getText()), Double.parseDouble(priceF.getText()), modeBox.getValue(), 3, 2, true, true, 500, 2);
                    case "Plot"   -> new Plot(id, titleF.getText(), addrF.getText(), cityF.getText(), Double.parseDouble(areaF.getText()), Double.parseDouble(priceF.getText()), modeBox.getValue(), "Residential", false, true, "North");
                    default       -> new Apartment(id, titleF.getText(), addrF.getText(), cityF.getText(), Double.parseDouble(areaF.getText()), Double.parseDouble(priceF.getText()), modeBox.getValue(), 2, 2, true, 1, true, true);
                };
                p.setImagePath(selectedImagePath);
                p.setOwner(client);
                if (agentBox.getValue() != null) {
                    p.setAssignedAgent(agentBox.getValue());
                }
                db.saveProperty(p);
                client.addOwnedProperty(p);

                NotificationCenter.getInstance().push(new notification.Notification(NotificationType.SYSTEM, "New Client Property: " + p.getTitle(), client.getEmail()));
                AlertHelper.showSuccess("Success", "Your property has been listed successfully.");
                showMyProperties();
            } catch (Exception ex) { AlertHelper.showError("Failed", ex.getMessage()); }
        });

        form.getChildren().addAll(split, new Separator(), submitBtn);
        setContent(title, form);
    }

    private void showOffers() {
        Label title = sectionTitle("My Negotiation History");
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
                    if (o.getOffererId() == client.getPersonId() && o.getStatus() == OfferStatus.PENDING) {
                        Button cancel = new Button("Cancel"); cancel.setStyle(StyleManager.dangerButton());
                        cancel.setOnAction(e -> { db.updateOfferStatus(o.getOfferId(), OfferStatus.CANCELLED); showOffers(); });
                        setGraphic(cancel);
                    } else if (o.getReceiverId() == client.getPersonId() && o.getStatus() == OfferStatus.PENDING) {
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
        table.setItems(FXCollections.observableArrayList(db.loadOffersForUser(client.getPersonId())));
        setContent(title, table);
    }

    private void showMessages() {
        Label title = sectionTitle("Communications Center");
        VBox list = new VBox(10);
        List<Integer> partners = db.loadRecentChatPartners(client.getPersonId());
        if (partners.isEmpty()) list.getChildren().add(new Label("No active chats. Explore properties to start one."));
        for (Integer pid : partners) {
            Person p = db.loadAllClients().stream().filter(c -> c.getPersonId() == pid).findFirst().orElse(null);
            if (p == null) p = db.loadAllAgents().stream().filter(a -> a.getPersonId() == pid).findFirst().orElse(null);
            if (p != null) {
                Button b = new Button("Chat with " + p.getName() + " (" + p.getRole() + ")");
                b.setStyle(StyleManager.card() + "-fx-cursor: hand;"); b.setPrefWidth(Double.MAX_VALUE);
                Person finalP = p; b.setOnAction(e -> new ChatWindow(client, finalP).show());
                list.getChildren().add(b);
            }
        }
        setContent(title, list);
    }

    // ─── HELPERS ───

    private VBox buildPremiumPropertyCard(Property p) {
        VBox card = new VBox(10);
        card.setPrefWidth(280);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 0;");
        card.setEffect(new DropShadow(10, Color.color(0, 0, 0, 0.05)));
        card.setCursor(javafx.scene.Cursor.HAND);
        card.setOnMouseClicked(e -> new PropertyDetailsWindow(p).show());
        
        ImageView iv = new ImageView();
        iv.setFitWidth(280);
        iv.setFitHeight(160);
        if (p.getImagePath() != null && new File(p.getImagePath()).exists()) {
            iv.setImage(new Image("file:" + p.getImagePath()));
        }
        
        StackPane imagePane = new StackPane(iv);
        if (p.getStatus() == PropertyStatus.SOLD) {
            Label soldBadge = new Label("SOLD OUT");
            soldBadge.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5;");
            StackPane.setAlignment(soldBadge, Pos.TOP_RIGHT);
            StackPane.setMargin(soldBadge, new Insets(10));
            imagePane.getChildren().add(soldBadge);
        }
        
        VBox info = new VBox(5);
        info.setPadding(new Insets(15));
        Label t = new Label(p.getTitle()); t.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;");
        
        double total = p.calculatePrice();
        double comm = total - p.getBasePrice();
        Label pr = new Label("PKR " + String.format("%.0f", total)); 
        pr.setStyle("-fx-text-fill: " + StyleManager.COLOR_SUCCESS + "; -fx-font-weight: bold;");
        
        if (comm > 0) {
            Label commLbl = new Label(String.format("(Includes PKR %.0f Commission)", comm));
            commLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: " + StyleManager.COLOR_TEXT_LIGHT + ";");
            info.getChildren().addAll(t, pr, commLbl);
        } else {
            info.getChildren().addAll(t, pr);
        }
        
        card.getChildren().addAll(imagePane, info);
        return card;
    }

    private VBox clientStatCard(String title, String val, String icon, String color) {
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

    private TextField styledField(String prompt) {
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
