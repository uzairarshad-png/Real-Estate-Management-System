package interfaces;

import model.Client;
import exceptions.BudgetExceededException;
import exceptions.InvalidPropertyException;

/**
 * Defines all transaction operations a property can support.
 * Implemented by: Apartment, House, Plot, Office, Shop
 */
public interface Transactable {

    /**
     * Process a purchase transaction — client buys this property.
     * @throws BudgetExceededException   if client budget < property price
     * @throws InvalidPropertyException  if property is not available for sale
     */
    void purchase(Client client) throws BudgetExceededException, InvalidPropertyException;

    /**
     * Process a rent transaction — client rents this property.
     * @param months number of months to rent
     * @throws BudgetExceededException   if client budget < total rent
     * @throws InvalidPropertyException  if property is not available for rent
     */
    void rent(Client client, int months) throws BudgetExceededException, InvalidPropertyException;

    /**
     * Process a sell transaction — client who owns this property sells it.
     * @throws InvalidPropertyException  if client does not own this property
     */
    void sellProperty(Client client) throws InvalidPropertyException;

    /**
     * Process a rent-out transaction — client who owns this property rents it out.
     * @throws InvalidPropertyException  if client does not own this property
     */
    void rentOut(Client client) throws InvalidPropertyException;

    /**
     * Calculate the final price of this property based on its type and attributes.
     * @return calculated price in PKR
     */
    double calculatePrice();
}
