package model;

import enums.OfferStatus;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a price offer made by a client on a property.
 */
public class Offer {
    private int offerId;
    private int propertyId;
    private int offererId;   // PersonId of the one making the offer
    private int receiverId;  // PersonId of the property owner/agent
    private double amount;
    private OfferStatus status;
    private String timestamp;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Offer(int offerId, int propertyId, int offererId, int receiverId, double amount) {
        this.offerId = offerId;
        this.propertyId = propertyId;
        this.offererId = offererId;
        this.receiverId = receiverId;
        this.amount = amount;
        this.status = OfferStatus.PENDING;
        this.timestamp = LocalDateTime.now().format(FORMATTER);
    }

    // Getters and Setters
    public int getOfferId() { return offerId; }
    public int getPropertyId() { return propertyId; }
    public int getOffererId() { return offererId; }
    public int getReceiverId() { return receiverId; }
    public double getAmount() { return amount; }
    public OfferStatus getStatus() { return status; }
    public String getTimestamp() { return timestamp; }

    public void setStatus(OfferStatus status) { this.status = status; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public void setAmount(double amount) { this.amount = amount; }
}
