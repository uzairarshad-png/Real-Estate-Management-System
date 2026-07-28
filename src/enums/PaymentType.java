package enums;

/**
 * Categorizes the type of payment being processed.
 */
public enum PaymentType {
    PURCHASE,           // Client paying to purchase a property
    SALE,               // Client selling a property to another client
    RENT,               // Client paying monthly rent
    RENT_OUT,           // Client receiving rent via rent-out deal
    AGENT_SALARY,       // Monthly salary disbursed to an agent
    AGENT_COMMISSION    // Commission paid to agent on a closed deal
}
