package model;

import enums.PropertyMode;

/**
 * Abstract subclass for all residential property types.
 * Subclasses: Apartment, House
 */
public abstract class ResidentialProperty extends Property {

    // ─── Fields ───────────────────────────────────────────────
    private int     bedrooms;
    private int     bathrooms;
    private boolean isFurnished;

    // ─── Constructor ──────────────────────────────────────────
    public ResidentialProperty(int propertyId, String title, String address,
                                String city, double area, double basePrice,
                                PropertyMode mode, int bedrooms,
                                int bathrooms, boolean isFurnished) {

        super(propertyId, title, address, city, area, basePrice, mode);
        validateBedrooms(bedrooms);
        validateBathrooms(bathrooms);

        this.bedrooms    = bedrooms;
        this.bathrooms   = bathrooms;
        this.isFurnished = isFurnished;
    }

    // ─── Validation ───────────────────────────────────────────
    private void validateBedrooms(int beds) {
        if (beds < 1)
            throw new IllegalArgumentException("Bedrooms must be at least 1.");
    }

    private void validateBathrooms(int baths) {
        if (baths < 1)
            throw new IllegalArgumentException("Bathrooms must be at least 1.");
    }

    // ─── Getters & Setters ────────────────────────────────────
    public int  getBedrooms()    { return bedrooms; }
    public int  getBathrooms()   { return bathrooms; }
    public boolean isFurnished() { return isFurnished; }

    public void setBedrooms(int bedrooms) {
        validateBedrooms(bedrooms);
        this.bedrooms = bedrooms;
    }

    public void setBathrooms(int bathrooms) {
        validateBathrooms(bathrooms);
        this.bathrooms = bathrooms;
    }

    public void setFurnished(boolean furnished) {
        this.isFurnished = furnished;
    }
}
