package model;

import enums.UserRole;
import interfaces.Payable;
import payment.Payment;
import exceptions.InsufficientBalanceException;
import exceptions.PaymentRejectedException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents an agent who manages property listings and
 * earns monthly salary plus per-deal commission.
 * Implements Payable for wallet management.
 */
public class Agent extends Person implements Payable {

    // ─── Fields ───────────────────────────────────────────────
    private String  licenseNumber;
    private double  monthlySalary;
    private double  commissionRate;   // percentage e.g. 2.5 = 2.5%
    private double  totalEarnings;
    private double  walletBalance;

    private final List<Property>    managedProperties;
    private final List<Transaction> closedDeals;
    private final List<Payment>     paymentHistory;

    // ─── Constructor ──────────────────────────────────────────
    public Agent(int personId, String name, String email,
                 String phone, String cnic, String passwordHash,
                 String licenseNumber, double monthlySalary, double commissionRate) {

        super(personId, name, email, phone, cnic, passwordHash, UserRole.AGENT);
        validateSalary(monthlySalary);
        validateCommissionRate(commissionRate);
        validateLicense(licenseNumber);

        this.licenseNumber     = licenseNumber;
        this.monthlySalary     = monthlySalary;
        this.commissionRate    = commissionRate;
        this.totalEarnings     = 0.0;
        this.walletBalance     = 0.0;

        this.managedProperties = new ArrayList<>();
        this.closedDeals       = new ArrayList<>();
        this.paymentHistory    = new ArrayList<>();
    }

    // ─── Validation ───────────────────────────────────────────
    private void validateSalary(double salary) {
        if (salary < 0)
            throw new IllegalArgumentException("Monthly salary cannot be negative.");
    }

    private void validateCommissionRate(double rate) {
        if (rate < 0 || rate > 100)
            throw new IllegalArgumentException("Commission rate must be between 0 and 100.");
    }

    private void validateLicense(String license) {
        if (license == null || license.trim().isEmpty())
            throw new IllegalArgumentException("License number cannot be empty.");
    }

    // ─── Payable Implementation ───────────────────────────────
    @Override
    public void processPayment(Payment payment)
            throws InsufficientBalanceException, PaymentRejectedException {

        if (payment == null)
            throw new IllegalArgumentException("Payment cannot be null.");
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

    // ─── Earnings ─────────────────────────────────────────────

    /**
     * Calculate the commission earned on a specific deal value.
     * @param dealValue total value of the transaction in PKR
     * @return commission amount in PKR
     */
    public double calculateCommission(double dealValue) {
        if (dealValue < 0)
            throw new IllegalArgumentException("Deal value cannot be negative.");
        return (commissionRate / 100.0) * dealValue;
    }

    /**
     * Credit monthly salary to agent's wallet.
     * Called by Admin via PaymentEngine.
     */
    public void receiveSalary(double amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Salary amount must be greater than zero.");
        walletBalance  += amount;
        totalEarnings  += amount;
    }

    /**
     * Credit commission from a closed deal to agent's wallet.
     * Called by Admin via PaymentEngine.
     */
    public void receiveCommission(double amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Commission amount must be greater than zero.");
        walletBalance  += amount;
        totalEarnings  += amount;
    }

    // ─── Property Management ──────────────────────────────────
    public void addProperty(Property p) {
        if (p == null)
            throw new IllegalArgumentException("Property cannot be null.");
        if (!managedProperties.contains(p))
            managedProperties.add(p);
    }

    public void removeProperty(Property p) {
        managedProperties.remove(p);
    }

    public void addClosedDeal(Transaction t) {
        if (t == null)
            throw new IllegalArgumentException("Transaction cannot be null.");
        closedDeals.add(t);
    }

    // ─── Abstract Override ────────────────────────────────────
    @Override
    public String getRole() {
        return "AGENT";
    }

    // ─── Getters ──────────────────────────────────────────────
    public String            getLicenseNumber()      { return licenseNumber; }
    public double            getMonthlySalary()      { return monthlySalary; }
    public double            getCommissionRate()     { return commissionRate; }
    public double            getTotalEarnings()      { return totalEarnings; }
    public double            getWalletBalance()      { return walletBalance; }
    public List<Property>    getManagedProperties()  { return Collections.unmodifiableList(managedProperties); }
    public List<Transaction> getClosedDeals()        { return Collections.unmodifiableList(closedDeals); }

    // ─── Setters ──────────────────────────────────────────────
    public void setMonthlySalary(double salary) {
        validateSalary(salary);
        this.monthlySalary = salary;
    }

    public void setCommissionRate(double rate) {
        validateCommissionRate(rate);
        this.commissionRate = rate;
    }

    public void setLicenseNumber(String license) {
        validateLicense(license);
        this.licenseNumber = license.trim();
    }

    public void setWalletBalance(double balance) {
        if (balance < 0)
            throw new IllegalArgumentException("Wallet balance cannot be negative.");
        this.walletBalance = balance;
    }

    public void setTotalEarnings(double earnings) {
        if (earnings < 0)
            throw new IllegalArgumentException("Total earnings cannot be negative.");
        this.totalEarnings = earnings;
    }

    // ─── toString ─────────────────────────────────────────────
    @Override
    public String toString() {
        return String.format(
            "[AGENT] ID: %d | %s | License: %s | Salary: PKR %.2f | Commission: %.1f%% | Wallet: PKR %.2f",
            getPersonId(), getName(), licenseNumber, monthlySalary, commissionRate, walletBalance
        );
    }
}
