package model;

import enums.PropertyMode;
import enums.PropertyStatus;

import interfaces.Transactable;

/**
 * Abstract base class for all property types in REMS.
 * Subclasses: ResidentialProperty (Apartment, House),
 *             CommercialProperty (Office, Shop), Plot
 */
public abstract class Property implements Transactable {

    // ─── Fields ───────────────────────────────────────────────
    private int            propertyId;
    private String         title;
    private String         address;
    private String         city;
    private double         area;          // in square feet
    private double         basePrice;     // in PKR
    private PropertyStatus status;
    private PropertyMode   mode;
    private Agent          assignedAgent;
    private Client         owner;
    private String         imagePath;     // Path to property image

    // ─── Constructor ──────────────────────────────────────────
    public Property(int propertyId, String title, String address,
                    String city, double area, double basePrice,
                    PropertyMode mode) {

        validateId(propertyId);
        validateTitle(title);
        validateAddress(address);
        validateCity(city);
        validateArea(area);
        validateBasePrice(basePrice);

        this.propertyId    = propertyId;
        this.title         = title.trim();
        this.address       = address.trim();
        this.city          = city.trim();
        this.area          = area;
        this.basePrice     = basePrice;
        this.mode          = mode;
        this.status        = PropertyStatus.AVAILABLE; // default listed directly
        this.assignedAgent = null;
        this.owner         = null;
    }

    // ─── Abstract Methods ─────────────────────────────────────

    /**
     * Calculate the final listed price based on property-specific attributes.
     * Overridden by each concrete subclass.
     */
    public abstract double calculatePrice();

    /**
     * Return the type label of this property.
     * e.g. "Apartment", "House", "Plot", "Office", "Shop"
     */
    public abstract String getPropertyType();

    // ─── Validation ───────────────────────────────────────────
    private void validateId(int id) {
        if (id < 0)
            throw new IllegalArgumentException("Property ID cannot be negative.");
    }

    private void validateTitle(String title) {
        if (title == null || title.trim().isEmpty())
            throw new IllegalArgumentException("Property title cannot be empty.");
    }

    private void validateAddress(String address) {
        if (address == null || address.trim().isEmpty())
            throw new IllegalArgumentException("Property address cannot be empty.");
    }

    private void validateCity(String city) {
        if (city == null || city.trim().isEmpty())
            throw new IllegalArgumentException("City cannot be empty.");
    }

    private void validateArea(double area) {
        if (area <= 0)
            throw new IllegalArgumentException("Area must be greater than zero.");
    }

    private void validateBasePrice(double price) {
        if (price <= 0)
            throw new IllegalArgumentException("Base price must be greater than zero.");
    }

    // ─── Getters ──────────────────────────────────────────────
    public int            getPropertyId()    { return propertyId; }
    public String         getTitle()         { return title; }
    public String         getAddress()       { return address; }
    public String         getCity()          { return city; }
    public double         getArea()          { return area; }
    public double         getBasePrice()     { return basePrice; }
    public PropertyStatus getStatus()        { return status; }
    public PropertyMode   getMode()          { return mode; }
    public Agent          getAssignedAgent() { return assignedAgent; }
    public Client         getOwner()         { return owner; }
    public String         getImagePath()     { return imagePath; }

    // ─── Setters ──────────────────────────────────────────────
    public void setTitle(String title) {
        validateTitle(title);
        this.title = title.trim();
    }

    public void setAddress(String address) {
        validateAddress(address);
        this.address = address.trim();
    }

    public void setCity(String city) {
        validateCity(city);
        this.city = city.trim();
    }

    public void setArea(double area) {
        validateArea(area);
        this.area = area;
    }

    public void setBasePrice(double price) {
        validateBasePrice(price);
        this.basePrice = price;
    }

    public void setStatus(PropertyStatus status) {
        if (status == null)
            throw new IllegalArgumentException("Status cannot be null.");
        this.status = status;
    }

    public void setMode(PropertyMode mode) {
        if (mode == null)
            throw new IllegalArgumentException("Mode cannot be null.");
        this.mode = mode;
    }

    public void setAssignedAgent(Agent agent) {
        this.assignedAgent = agent;
    }

    public void setOwner(Client owner) {
        this.owner = owner;
    }

    public void setImagePath(String path) {
        this.imagePath = path;
    }

    // ─── Searchable Helper ────────────────────────────────────

    /**
     * Used by Searchable implementations to check type + price filter.
     */
    protected boolean baseMatchesFilter(String type, double maxPrice) {
        boolean typeMatch  = (type == null || type.equalsIgnoreCase("Any")
                               || type.equalsIgnoreCase(getPropertyType()));
        boolean priceMatch = calculatePrice() <= maxPrice;
        return typeMatch && priceMatch;
    }

    // ─── toString ─────────────────────────────────────────────
    @Override
    public String toString() {
        return String.format(
            "[%s] ID: %d | %s | %s, %s | %.0f sqft | PKR %.2f | %s | %s",
            getPropertyType(), propertyId, title, address, city,
            area, calculatePrice(), status, mode
        );
    }
}
