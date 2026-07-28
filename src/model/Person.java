package model;

import enums.AccountStatus;
import enums.UserRole;

/**
 * Abstract base class for all users in REMS.
 * Subclasses: Admin, Agent, Client
 */
public abstract class Person {

    // ─── Fields ───────────────────────────────────────────────
    private int           personId;
    private String        name;
    private String        email;
    private String        phone;
    private String        cnic;
    protected String      passwordHash;   // protected so subclasses can read but not expose
    private UserRole      role;
    private AccountStatus accountStatus;

    // ─── Constructor ──────────────────────────────────────────
    public Person(int personId, String name, String email,
                  String phone, String cnic,
                  String passwordHash, UserRole role) {

        this.personId      = personId;
        this.name          = name;
        this.email         = email;
        this.phone         = phone;
        this.cnic          = cnic;
        this.passwordHash  = passwordHash;
        this.role          = role;
        this.accountStatus = AccountStatus.ACTIVE; // default on creation
    }

    // ─── Abstract Methods ─────────────────────────────────────

    /**
     * Returns the role label for this user.
     * Overridden by Admin, Agent, Client.
     */
    public abstract String getRole();

    // ─── Getters ──────────────────────────────────────────────
    public int           getPersonId()      { return personId; }
    public String        getName()          { return name; }
    public String        getEmail()         { return email; }
    public String        getPhone()         { return phone; }
    public String        getCnic()          { return cnic; }
    public String        getPasswordHash()  { return passwordHash; }
    public UserRole      getUserRole()      { return role; }
    public AccountStatus getAccountStatus() { return accountStatus; }

    // ─── Setters ──────────────────────────────────────────────
    public void setName(String name) {
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Name cannot be empty.");
        this.name = name.trim();
    }

    public void setEmail(String email) {
        if (email == null || email.trim().isEmpty())
            throw new IllegalArgumentException("Email cannot be empty.");
        this.email = email.trim().toLowerCase();
    }

    public void setPhone(String phone) {
        if (phone == null || phone.trim().isEmpty())
            throw new IllegalArgumentException("Phone cannot be empty.");
        this.phone = phone.trim();
    }

    public void setCnic(String cnic) {
        if (cnic == null || cnic.trim().isEmpty())
            throw new IllegalArgumentException("CNIC cannot be empty.");
        this.cnic = cnic.trim();
    }

    public void setPasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isEmpty())
            throw new IllegalArgumentException("Password hash cannot be empty.");
        this.passwordHash = passwordHash;
    }

    public void setAccountStatus(AccountStatus status) {
        if (status == null)
            throw new IllegalArgumentException("Account status cannot be null.");
        this.accountStatus = status;
    }

    // ─── toString ─────────────────────────────────────────────
    @Override
    public String toString() {
        return String.format("[%s] ID: %d | Name: %s | Email: %s | Status: %s",
                getRole(), personId, name, email, accountStatus);
    }
}
