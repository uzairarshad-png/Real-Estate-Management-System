package interfaces;

/**
 * Defines search and filter capabilities for property listings.
 * Implemented by: Apartment, House, Plot, Office, Shop
 */
public interface Searchable {

    /**
     * Check whether this property matches the given search filter.
     * @param type     property type string e.g. "Apartment", "House", "Plot"
     * @param maxPrice maximum price the searcher is willing to pay
     * @return true if property matches both criteria
     */
    boolean matchesFilter(String type, double maxPrice);

    /**
     * Returns a concise one-line summary of this property
     * for display in search results.
     * Example: "Apartment | DHA Phase 5 | 3 Bed | PKR 12,500,000 | AVAILABLE"
     * @return formatted summary string
     */
    String getSummary();
}
