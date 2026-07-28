package gui;

import database.DatabaseManager;
import model.ChatMessage;
import model.Person;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A persistent chat window for real-time (polling) communication.
 */
public class ChatWindow {

    private final Person currentUser;
    private final Person targetUser;
    private final DatabaseManager db;
    private final VBox chatArea;
    private final ScrollPane scroll;
    private Timer poller;

    public ChatWindow(Person currentUser, Person targetUser) {
        this.currentUser = currentUser;
        this.targetUser  = targetUser;
        this.db          = DatabaseManager.getInstance();
        this.chatArea    = new VBox(10);
        this.scroll      = new ScrollPane(chatArea);
    }

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("Chat with " + targetUser.getName());
        stage.setMinWidth(400);
        stage.setMinHeight(500);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + StyleManager.COLOR_BG + ";");

        // Header
        HBox header = new HBox(10);
        header.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-color: #eee; -fx-border-width: 0 0 1 0;");
        Label name = new Label("💬 " + targetUser.getName());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        header.getChildren().add(name);
        root.setTop(header);

        // Chat Area
        chatArea.setPadding(new Insets(15));
        scroll.setFitToWidth(true);
        scroll.setStyle(StyleManager.scrollPane());
        root.setCenter(scroll);

        // Input Area
        HBox inputArea = new HBox(10);
        inputArea.setPadding(new Insets(15));
        inputArea.setStyle("-fx-background-color: white;");
        TextField input = new TextField();
        input.setPromptText("Type a message...");
        input.setStyle(StyleManager.textField());
        HBox.setHgrow(input, Priority.ALWAYS);
        
        Button sendBtn = new Button("Send");
        sendBtn.setStyle(StyleManager.primaryButton());
        sendBtn.setOnAction(e -> {
            String text = input.getText().trim();
            if (!text.isEmpty()) {
                ChatMessage msg = new ChatMessage(db.getNextMessageId(), currentUser.getPersonId(), targetUser.getPersonId(), text);
                db.saveChatMessage(msg);
                input.clear();
                refreshChat();
            }
        });
        
        inputArea.getChildren().addAll(input, sendBtn);
        root.setBottom(inputArea);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> { if (poller != null) poller.cancel(); });
        stage.show();

        startPolling();
    }

    private void startPolling() {
        poller = new Timer(true);
        poller.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> refreshChat());
            }
        }, 0, 2000); // Poll every 2 seconds
    }

    private void refreshChat() {
        List<ChatMessage> history = db.loadChatHistory(currentUser.getPersonId(), targetUser.getPersonId());
        chatArea.getChildren().clear();
        for (ChatMessage m : history) {
            boolean isMine = m.getSenderId() == currentUser.getPersonId();
            Label l = new Label(m.getContent());
            l.setWrapText(true);
            l.setMaxWidth(250);
            l.setPadding(new Insets(8, 12, 8, 12));
            
            VBox wrapper = new VBox(l);
            wrapper.setPrefWidth(Double.MAX_VALUE);
            
            if (isMine) {
                l.setStyle("-fx-background-color: " + StyleManager.COLOR_ACCENT + "; -fx-text-fill: white; -fx-background-radius: 15 15 2 15;");
                wrapper.setAlignment(Pos.CENTER_RIGHT);
            } else {
                l.setStyle("-fx-background-color: #f1f1f1; -fx-text-fill: #333; -fx-background-radius: 15 15 15 2;");
                wrapper.setAlignment(Pos.CENTER_LEFT);
            }
            chatArea.getChildren().add(wrapper);
        }
        scroll.setVvalue(1.0); // Scroll to bottom
    }
}
