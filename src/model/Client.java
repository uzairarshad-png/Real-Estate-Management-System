package model;

import enums.UserRole;
import enums.AccountStatus;
import interfaces.Payable;
import payment.Payment;
import exceptions.InsufficientBalanceException;
import exceptions.PaymentRejectedException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a client user who can buy, sell, rent,
 * and rent out properties. Implements Payable for wallet management.
 */
public class Client extends Person implements Payable {

    // ─── Fields ───────────────────────────────────────────────
    private double         budget;
    private double         walletBalance;
    private String         preferredPropertyType;
    private boolean        pendingAdminApproval;

    private final List<Property>    ownedProperties;
    private final List<Property>    rentedProperties;
    private final List<Transaction> transactions;
    private final List<Payment>     paymentHistory;

    // ─── Constructor ──────────────────────────────────────────
    public Client(int personId, String name, String email,
                  String phone, String cnic, String passwordHash,
                  double budget) {

        super(personId, name, email, phone, cnic, passwordHash, UserRole.CLIENT);
        validateBudget(budget);

        this.budget                = budget;
        this.walletBalance         = budget;   // wallet starts equal to budget
        this.preferredPropertyType = "Any";
        this.pendingAdminApproval  = false;

        this.ownedProperties  = new ArrayList<>();
        this.rentedProperties = new ArrayList<>();
        this.transactions     = new ArrayList<>();
        this.paymentHistory   = new ArrayList<>();
    }

    // ─── Validation ───────────────────────────────────────────
    private void validateBudget(double budget) {
        if (budget < 0)
            throw new IllegalArgumentException("Budget cannot be negative.");
    }

    // ─── Payable Implementation ───────────────────────────────
    @Override
    public void processPayment(Payment payment)
            throws InsufficientBalanceException, PaymentRejectedException {

        if (payment == null)
            throw new IllegalArgumentException("Payment cannot be null.");

        if (walletBalance < payment.getAmount())
            throw new InsufficientBalanceException(walletBalance, payment.getAmount());

        walletBalance -= payment.getAmount();
        paymentHistory.add(payment);
    }

    @Override
    public double getBalance() {
        return walletBalance;
    }

    @Override
    public List<Payment> getPaymentHistory() {
        return Collections.unmodifiableList(paymentHistory);
    }

    // ─── Property Management ──────────────────────────────────
    public void addOwnedProperty(Property p) {
        if (p == null)
            throw new IllegalArgumentException("Property cannot be null.");
        if (!ownedProperties.contains(p))
            ownedProperties.add(p);
    }

    public void removeOwnedProperty(Property p) {
        ownedProperties.remove(p);
    }

    public void addRentedProperty(Property p) {
        if (p == null)
            throw new IllegalArgumentException("Property cannot be null.");
        if (!rentedProperties.contains(p))
            rentedProperties.add(p);
    }

    public void removeRentedProperty(Property p) {
        rentedProperties.remove(p);
    }

    // ─── Transaction Management ───────────────────────────────
    public void addTransaction(Transaction t) {
        if (t == null)
            throw new IllegalArgumentException("Transaction cannot be null.");
        transactions.add(t);
    }

    // ─── Wallet ───────────────────────────────────────────────
    public void makePayment(double amount) throws InsufficientBalanceException {
        if (amount <= 0)
            throw new IllegalArgumentException("Payment amount must be greater than zero.");
        if (walletBalance < amount)
            throw new InsufficientBalanceException(walletBalance, amount);
        walletBalance -= amount;
    }

    public void receivePayment(double amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Received amount must be greater than zero.");
        walletBalance += amount;
    }

    public void topUpWallet(double amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Top-up amount must be greater than zero.");
        walletBalance += amount;
        budget        += amount;
    }

    // ─── Abstract Override ────────────────────────────────────
    @Override
    public String getRole() {
        return "CLIENT";
    }

    // ─── Getters & Setters ────────────────────────────────────
    public double getBudget()                     { return budget; }
    public double getWalletBalance()              { return walletBalance; }
    public String getPreferredPropertyType()      { return preferredPropertyType; }
    public boolean isPendingAdminApproval()       { return pendingAdminApproval; }
    public List<Property> getOwnedProperties()    { return Collections.unmodifiableList(ownedProperties); }
    public List<Property> getRentedProperties()   { return Collections.unmodifiableList(rentedProperties); }
    public List<Transaction> getTransactions()    { return Collections.unmodifiableList(transactions); }

    public void setBudget(double budget) {
        validateBudget(budget);
        this.budget = budget;
    }

    public void setWalletBalance(double walletBalance) {
        if (walletBalance < 0)
            throw new IllegalArgumentException("Wallet balance cannot be negative.");
        this.walletBalance = walletBalance;
    }

    public void setPreferredPropertyType(String type) {
        this.preferredPropertyType = (type == null || type.trim().isEmpty()) ? "Any" : type.trim();
    }

    public void setPendingAdminApproval(boolean pending) {
        this.pendingAdminApproval = pending;
        if (pending)
            setAccountStatus(AccountStatus.PENDING_REGISTRATION);
    }

    // ─── toString ─────────────────────────────────────────────
    @Override
    public String toString() {
        return String.format(
            "[CLIENT] ID: %d | %s | Email: %s | Budget: PKR %.2f | Wallet: PKR %.2f | Status: %s",
            getPersonId(), getName(), getEmail(), budget, walletBalance, getAccountStatus()
        );
    }
}
