package model;

import enums.PropertyMode;

/**
 * Abstract subclass for all commercial property types.
 * Subclasses: Office, Shop
 */
public abstract class CommercialProperty extends Property {

    // ─── Fields ───────────────────────────────────────────────
    private String  businessZone;
    private int     parkingSpots;

    // ─── Constructor ──────────────────────────────────────────
    public CommercialProperty(int propertyId, String title, String address,
                               String city, double area, double basePrice,
                               PropertyMode mode, String businessZone,
                               int parkingSpots) {

        super(propertyId, title, address, city, area, basePrice, mode);
        validateBusinessZone(businessZone);
        validateParkingSpots(parkingSpots);

        this.businessZone = businessZone.trim();
        this.parkingSpots = parkingSpots;
    }

    // ─── Validation ───────────────────────────────────────────
    private void validateBusinessZone(String zone) {
        if (zone == null || zone.trim().isEmpty())
            throw new IllegalArgumentException("Business zone cannot be empty.");
    }

    private void validateParkingSpots(int spots) {
        if (spots < 0)
            throw new IllegalArgumentException("Parking spots cannot be negative.");
    }

    // ─── Getters & Setters ────────────────────────────────────
    public String getBusinessZone() { return businessZone; }
    public int    getParkingSpots() { return parkingSpots; }

    public void setBusinessZone(String zone) {
        validateBusinessZone(zone);
        this.businessZone = zone.trim();
    }

    public void setParkingSpots(int spots) {
        validateParkingSpots(spots);
        this.parkingSpots = spots;
    }
}
