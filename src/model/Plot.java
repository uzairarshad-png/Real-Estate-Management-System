package model;

import enums.PropertyMode;
import enums.PropertyStatus;
import interfaces.Transactable;
import interfaces.Searchable;
import exceptions.BudgetExceededException;
import exceptions.InvalidPropertyException;

/**
 * Represents a plot/land property.
 * Supports: Purchase, Sell only (no renting for plots).
 */
public class Plot extends Property
        implements Transactable, Searchable {

    // ─── Fields ───────────────────────────────────────────────
    private String  plotType;       // e.g. "Residential", "Commercial", "Industrial"
    private boolean isCornerPlot;
    private boolean isOnMainRoad;
    private String  facing;         // e.g. "East", "West", "North", "South"

    // ─── Constructor ──────────────────────────────────────────
    public Plot(int propertyId, String title, String address,
                String city, double area, double basePrice,
                PropertyMode mode, String plotType,
                boolean isCornerPlot, boolean isOnMainRoad, String facing) {

        super(propertyId, title, address, city, area, basePrice, mode);
        validatePlotType(plotType);
        validateFacing(facing);

        this.plotType     = plotType.trim();
        this.isCornerPlot = isCornerPlot;
        this.isOnMainRoad = isOnMainRoad;
        this.facing       = facing.trim();
    }

    // ─── Validation ───────────────────────────────────────────
    private void validatePlotType(String type) {
        if (type == null || type.trim().isEmpty())
            throw new IllegalArgumentException("Plot type cannot be empty.");
    }

    private void validateFacing(String facing) {
        if (facing == null || facing.trim().isEmpty())
            throw new IllegalArgumentException("Plot facing direction cannot be empty.");
    }

    // ─── Property Abstract Implementations ────────────────────
    @Override
    public double calculatePrice() {
        double price = getBasePrice();
        // Corner plot premium
        if (isCornerPlot)  price += price * 0.10;
        // Main road premium
        if (isOnMainRoad)  price += price * 0.08;
        // South-facing slight reduction (less preferred in Pakistan)
        if ("South".equalsIgnoreCase(facing)) price -= price * 0.02;
        return price;
    }

    @Override
    public String getPropertyType() {
        return "Plot";
    }

    // ─── Transactable Implementation ──────────────────────────
    @Override
    public void purchase(Client client)
            throws BudgetExceededException, InvalidPropertyException {

        if (client == null)
            throw new IllegalArgumentException("Client cannot be null.");
        if (getStatus() != PropertyStatus.AVAILABLE)
            throw new InvalidPropertyException("status",
                    "Plot is not available for purchase. Current status: " + getStatus());
        if (getMode() != PropertyMode.FOR_SALE)
            throw new InvalidPropertyException("mode", "Plot is not listed for sale.");

        double price = calculatePrice();
        if (client.getBudget() < price)
            throw new BudgetExceededException(client.getBudget(), price);

        setStatus(PropertyStatus.SOLD);
        setOwner(client);
        client.addOwnedProperty(this);
    }

    @Override
    public void rent(Client client, int months)
            throws BudgetExceededException, InvalidPropertyException {
        throw new InvalidPropertyException("operation",
                "Plots cannot be rented. Only purchase or sale is available.");
    }

    @Override
    public void sellProperty(Client client) throws InvalidPropertyException {
        if (client == null)
            throw new IllegalArgumentException("Client cannot be null.");
        if (!client.getOwnedProperties().contains(this))
            throw new InvalidPropertyException("owner", "Client does not own this plot.");

        setStatus(PropertyStatus.AVAILABLE);
        setMode(PropertyMode.FOR_SALE);
        client.removeOwnedProperty(this);
        setOwner(null);
    }

    @Override
    public void rentOut(Client client) throws InvalidPropertyException {
        throw new InvalidPropertyException("operation",
                "Plots cannot be rented out. Only purchase or sale is available.");
    }

    // ─── Searchable Implementation ────────────────────────────
    @Override
    public boolean matchesFilter(String type, double maxPrice) {
        return baseMatchesFilter(type, maxPrice)
               && getStatus() == PropertyStatus.AVAILABLE;
    }

    @Override
    public String getSummary() {
        return String.format(
            "Plot | %s, %s | %.0f sqft | %s | %s | %s | Facing: %s | PKR %.2f | %s",
            getAddress(), getCity(), getArea(), plotType,
            isCornerPlot  ? "Corner"   : "Non-Corner",
            isOnMainRoad  ? "Main Road" : "Side Road",
            facing, calculatePrice(), getStatus()
        );
    }

    // ─── Getters & Setters ────────────────────────────────────
    public String  getPlotType()    { return plotType; }
    public boolean isCornerPlot()   { return isCornerPlot; }
    public boolean isOnMainRoad()   { return isOnMainRoad; }
    public String  getFacing()      { return facing; }

    public void setPlotType(String type) {
        validatePlotType(type);
        this.plotType = type.trim();
    }

    public void setCornerPlot(boolean corner)   { this.isCornerPlot = corner; }
    public void setOnMainRoad(boolean mainRoad) { this.isOnMainRoad = mainRoad; }

    public void setFacing(String facing) {
        validateFacing(facing);
        this.facing = facing.trim();
    }
}
