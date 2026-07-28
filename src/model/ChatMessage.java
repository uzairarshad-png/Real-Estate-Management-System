package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a single message in a persistent chat thread.
 */
public class ChatMessage {
    private int messageId;
    private int senderId;
    private int receiverId;
    private String content;
    private String timestamp;
    private boolean isRead;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ChatMessage(int messageId, int senderId, int receiverId, String content) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.timestamp = LocalDateTime.now().format(FORMATTER);
        this.isRead = false;
    }

    // Getters and Setters
    public int getMessageId() { return messageId; }
    public int getSenderId() { return senderId; }
    public int getReceiverId() { return receiverId; }
    public String getContent() { return content; }
    public String getTimestamp() { return timestamp; }
    public boolean isRead() { return isRead; }

    public void setRead(boolean read) { isRead = read; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
