package enums;

/**
 * Represents the current status of a price offer/negotiation.
 */
public enum OfferStatus {
    PENDING,        // Offer made, waiting for response
    ACCEPTED,       // Owner accepted the offer
    REJECTED,       // Owner rejected the offer
    COUNTERED,      // Owner proposed a different price
    CANCELLED       // Offerer withdrew the offer
}
