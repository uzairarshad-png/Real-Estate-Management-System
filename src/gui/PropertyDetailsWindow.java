package gui;

import model.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import database.DatabaseManager;
import enums.*;
import security.Session;
import java.io.File;

/**
 * A new window feature to display detailed property information.
 * Supports resizing, maximizing, and minimizing (Window Control Feature).
 */
public class PropertyDetailsWindow {

    private final Property property;

    public PropertyDetailsWindow(Property property) {
        this.property = property;
    }

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("Property Details — " + property.getTitle());
        stage.setMinWidth(600);
        stage.setMinHeight(500);
        stage.setResizable(true); // Feature: Adjust/Maximize/Minimize

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + StyleManager.COLOR_BG + ";");

        // Top Header
        HBox header = new HBox(15);
        header.setStyle("-fx-background-color: white; -fx-padding: 20;");
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label(property.getTitle());
        title.setStyle(StyleManager.titleLabel());
        header.getChildren().add(title);
        if (property.getStatus() == PropertyStatus.SOLD) {
            Label soldBadge = new Label("SOLD OUT");
            soldBadge.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 15; -fx-background-radius: 5;");
            header.getChildren().add(soldBadge);
        }
        root.setTop(header);

        // Center Content
        ScrollPane scroll = new ScrollPane();
        VBox content = new VBox(25);
        content.setPadding(new Insets(30));
        content.setAlignment(Pos.TOP_CENTER);
        
        // Image Display
        ImageView iv = new ImageView();
        iv.setFitWidth(500);
        iv.setPreserveRatio(true);
        if (property.getImagePath() != null && new File(property.getImagePath()).exists()) {
            iv.setImage(new Image("file:" + property.getImagePath()));
        } else {
            // Optional: Placeholder for no image
            Label noImg = new Label("No Image Available");
            noImg.setStyle("-fx-font-size: 14px; -fx-text-fill: grey;");
            content.getChildren().add(noImg);
        }
        
        // Info Grid
        GridPane grid = new GridPane();
        grid.setHgap(40);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        grid.setStyle(StyleManager.card());
        
        addInfoRow(grid, 0, "Type:", property.getPropertyType());
        addInfoRow(grid, 1, "Address:", property.getAddress());
        addInfoRow(grid, 2, "City:", property.getCity());
        addInfoRow(grid, 3, "Area:", property.getArea() + " sq ft");
        
        double total = property.calculatePrice();
        double base = property.getBasePrice();
        double comm = total - base;
        
        addInfoRow(grid, 4, "Base Price:", "PKR " + String.format("%.0f", base));
        if (comm > 0) {
            addInfoRow(grid, 5, "Agent Commission:", "PKR " + String.format("%.0f", comm));
            addInfoRow(grid, 6, "Total Price:", "PKR " + String.format("%.0f", total));
            addInfoRow(grid, 7, "Status:", property.getStatus().name());
            addInfoRow(grid, 8, "Agent:", (property.getAssignedAgent() != null ? property.getAssignedAgent().getName() : "N/A"));
        } else {
            addInfoRow(grid, 5, "Total Price:", "PKR " + String.format("%.0f", total));
            addInfoRow(grid, 6, "Status:", property.getStatus().name());
            addInfoRow(grid, 7, "Agent:", (property.getAssignedAgent() != null ? property.getAssignedAgent().getName() : "N/A"));
        }

        content.getChildren().addAll(iv, grid);
        scroll.setContent(content);
        scroll.setFitToWidth(true);
        scroll.setStyle(StyleManager.scrollPane());
        
        root.setCenter(scroll);

        // Bottom Actions
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(20));
        footer.setSpacing(10);

        Button chatBtn = new Button("💬 Chat");
        chatBtn.setStyle(StyleManager.secondaryButton());
        chatBtn.setOnAction(e -> {
            Person currentUser = Session.getInstance().getCurrentUser();
            Person target = property.getAssignedAgent();
            if (target == null) target = property.getOwner();
            if (target != null && target.getPersonId() != currentUser.getPersonId()) {
                new ChatWindow(currentUser, target).show();
            } else {
                AlertHelper.showError("Chat", "No agent or owner assigned, or you are the owner.");
            }
        });

        Button offerBtn = new Button("🤝 Make Offer");
        offerBtn.setStyle(StyleManager.primaryButton());
        offerBtn.setOnAction(e -> handleMakeOffer());
        
        if (property.getStatus() == PropertyStatus.SOLD) {
            offerBtn.setDisable(true);
            offerBtn.setText("Property Sold");
        }
        
        // Only show "Make Offer" to clients who don't own the property
        Person current = Session.getInstance().getCurrentUser();
        boolean isOwner = (property.getOwner() != null && property.getOwner().getPersonId() == current.getPersonId());
        if (current.getUserRole() == UserRole.CLIENT && !isOwner) {
            footer.getChildren().add(offerBtn);
        }
        
        footer.getChildren().addAll(chatBtn);

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        footer.getChildren().add(spacer);

        Button closeBtn = new Button("Close View");
        closeBtn.setStyle(StyleManager.secondaryButton());
        closeBtn.setOnAction(e -> stage.close());
        footer.getChildren().add(closeBtn);
        root.setBottom(footer);

        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    private void addInfoRow(GridPane grid, int row, String label, String value) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: " + StyleManager.COLOR_TEXT_LIGHT + ";");
        Label val = new Label(value);
        val.setStyle("-fx-font-size: 14px; -fx-text-fill: " + StyleManager.COLOR_TEXT + ";");
        grid.add(lbl, 0, row);
        grid.add(val, 1, row);
    }

    private void handleMakeOffer() {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(property.calculatePrice()));
        dialog.setTitle("Submit Price Offer");
        dialog.setHeaderText("Negotiate for " + property.getTitle());
        dialog.setContentText("Enter your offer amount (PKR):");
        dialog.showAndWait().ifPresent(val -> {
            try {
                double amount = Double.parseDouble(val);
                Person current = Session.getInstance().getCurrentUser();
                Person receiver = property.getAssignedAgent();
                if (receiver == null) receiver = property.getOwner();
                
                if (receiver == null) {
                    AlertHelper.showError("Offer Failed", "No agent or owner to receive the offer.");
                    return;
                }

                DatabaseManager db = DatabaseManager.getInstance();
                Offer offer = new Offer(db.getNextOfferId(), property.getPropertyId(), current.getPersonId(), receiver.getPersonId(), amount);
                db.saveOffer(offer);
                
                AlertHelper.showSuccess("Offer Sent", "Your offer of PKR " + amount + " has been submitted for review.");
            } catch (Exception ex) {
                AlertHelper.showError("Invalid Amount", "Please enter a numeric value.");
            }
        });
    }
}
