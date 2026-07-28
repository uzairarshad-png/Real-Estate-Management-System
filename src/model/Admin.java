package model;

import enums.UserRole;
import enums.AccountStatus;
import enums.RequestStatus;
import interfaces.Notifiable;
import notification.Notification;
import notification.RegistrationRequest;
import notification.PasswordResetRequest;
import notification.NotificationCenter;
import payment.Payment;
import payment.PaymentEngine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the Admin user — hardcoded credentials, full system control.
 * Manages payments, approvals, profile creation, and notification center.
 * Implements Notifiable to receive all system alerts.
 */
public class Admin extends Person implements Notifiable {

    // ─── Hardcoded Credentials ────────────────────────────────
    // Password is SHA-256 hash of "Admin@123"
    public static final String ADMIN_USERNAME  = "admin@rems.com";
    public static final String ADMIN_PASS_HASH =
        "e86f78a8a3caf0b60d8e74e5942aa6d86dc150cd3c03338aef25b7d2d7e3acc7";

    // ─── Fields ───────────────────────────────────────────────
    private final NotificationCenter         notificationCenter;
    private final List<RegistrationRequest>  pendingRequests;
    private final List<Notification>         notifications;

    // ─── Constructor ──────────────────────────────────────────
    public Admin() {
        super(0, "System Admin", ADMIN_USERNAME,
              "0300-0000000", "00000-0000000-0",
              ADMIN_PASS_HASH, UserRole.ADMIN);

        this.notificationCenter = NotificationCenter.getInstance();
        this.pendingRequests    = new ArrayList<>();
        this.notifications      = new ArrayList<>();
    }

    // ─── Notifiable Implementation ────────────────────────────
    @Override
    public void sendNotification(Notification notification) {
        if (notification == null)
            throw new IllegalArgumentException("Notification cannot be null.");
        notifications.add(notification);
        notificationCenter.push(notification);
    }

    @Override
    public List<Notification> getNotifications() {
        return Collections.unmodifiableList(notifications);
    }

    // ─── Login Approval ───────────────────────────────────────

    /**
     * Manually unlock a locked account — allows user to log in again.
     * Called from Admin Dashboard after reviewing a lockout notification.
     */
    public boolean approveLogin(Person user) {
        if (user == null)
            throw new IllegalArgumentException("User cannot be null.");
        if (user.getAccountStatus() == AccountStatus.LOCKED_PENDING_ADMIN) {
            user.setAccountStatus(AccountStatus.ACTIVE);
            return true;
        }
        return false;
    }

    // ─── Registration Approval ────────────────────────────────

    /**
     * Approve a pending registration from the Login Page.
     * Creates and activates the user profile.
     */
    public boolean approveRegistration(RegistrationRequest request) {
        if (request == null)
            throw new IllegalArgumentException("Request cannot be null.");
        if (request.getStatus() != RequestStatus.PENDING)
            return false;

        request.approve();
        pendingRequests.remove(request);
        return true;
    }

    /**
     * Reject a pending registration request.
     */
    public boolean rejectRegistration(RegistrationRequest request) {
        if (request == null)
            throw new IllegalArgumentException("Request cannot be null.");
        if (request.getStatus() != RequestStatus.PENDING)
            return false;

        request.reject();
        pendingRequests.remove(request);
        return true;
    }

    public void addPendingRequest(RegistrationRequest request) {
        if (request != null && !pendingRequests.contains(request))
            pendingRequests.add(request);
    }

    // ─── Password Reset ───────────────────────────────────────

    /**
     * Approve a password reset — assign new hashed password and unlock account.
     */
    public boolean approvePasswordReset(PasswordResetRequest request, String newHash, Person user) {
        if (request == null || newHash == null || newHash.isEmpty() || user == null)
            throw new IllegalArgumentException("Invalid arguments for password reset.");
        if (request.getStatus() != RequestStatus.PENDING)
            return false;

        request.approve(newHash);
        user.setPasswordHash(newHash);
        user.setAccountStatus(AccountStatus.ACTIVE);
        return true;
    }

    public boolean rejectPasswordReset(PasswordResetRequest request) {
        if (request == null)
            throw new IllegalArgumentException("Request cannot be null.");
        if (request.getStatus() != RequestStatus.PENDING)
            return false;
        request.reject();
        return true;
    }

    // ─── Payment Management ───────────────────────────────────

    /**
     * Approve a pending payment — triggers disbursement via PaymentEngine.
     */
    public boolean approvePayment(int paymentId) {
        PaymentEngine engine = PaymentEngine.getInstance();
        return engine.approvePayment(paymentId);
    }

    /**
     * Reject a pending payment.
     */
    public boolean rejectPayment(int paymentId) {
        PaymentEngine engine = PaymentEngine.getInstance();
        return engine.rejectPayment(paymentId);
    }

    /**
     * Disburse monthly salary to an agent.
     */
    public void processAgentSalary(Agent agent) {
        if (agent == null)
            throw new IllegalArgumentException("Agent cannot be null.");
        PaymentEngine.getInstance().disburseSalary(agent, agent.getMonthlySalary());
    }

    /**
     * Disburse commission to agent after a deal closes.
     */
    public void processCommission(Agent agent, Transaction transaction) {
        if (agent == null || transaction == null)
            throw new IllegalArgumentException("Agent and transaction cannot be null.");
        PaymentEngine.getInstance().disburseCommission(agent, transaction);
    }

    // ─── Profile Creation ─────────────────────────────────────

    /**
     * Admin directly creates a profile — account is immediately ACTIVE.
     * Used for Agent and Client profiles created from Admin Panel.
     */
    public void createProfile(Person person) {
        if (person == null)
            throw new IllegalArgumentException("Person cannot be null.");
        person.setAccountStatus(AccountStatus.ACTIVE);
    }

    // ─── Abstract Override ────────────────────────────────────
    @Override
    public String getRole() {
        return "ADMIN";
    }

    // ─── Getters ──────────────────────────────────────────────
    public List<RegistrationRequest> getPendingRequests() {
        return Collections.unmodifiableList(pendingRequests);
    }

    public NotificationCenter getNotificationCenter() {
        return notificationCenter;
    }

    // ─── toString ─────────────────────────────────────────────
    @Override
    public String toString() {
        return String.format(
            "[ADMIN] %s | Notifications: %d | Pending Requests: %d",
            getName(),
            notificationCenter.getUnreadCount(),
            pendingRequests.size()
        );
    }
}
