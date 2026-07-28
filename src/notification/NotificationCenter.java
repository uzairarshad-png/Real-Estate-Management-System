package notification;

import enums.NotificationType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Singleton Notification Center — central hub for all Admin alerts.
 *
 * All system events (login failures, unknown IDs, registration
 * requests, payment requests, password resets) push here.
 * Admin Dashboard reads from this queue.
 */
public class NotificationCenter {

    // ─── Singleton ────────────────────────────────────────────
    private static NotificationCenter instance;

    // ─── State ────────────────────────────────────────────────
    private final List<Notification> queue;

    // ─── Private Constructor ──────────────────────────────────
    private NotificationCenter() {
        this.queue = new ArrayList<>();
    }

    public static NotificationCenter getInstance() {
        if (instance == null) instance = new NotificationCenter();
        return instance;
    }

    // ─── Push ─────────────────────────────────────────────────

    /**
     * Add a new notification to the queue.
     * @throws IllegalArgumentException if notification is null
     */
    public void push(Notification notification) {
        if (notification == null)
            throw new IllegalArgumentException("Notification cannot be null.");
        queue.add(notification);
    }

    // ─── Read ─────────────────────────────────────────────────

    /** Get all notifications (read + unread). */
    public List<Notification> getAll() {
        return Collections.unmodifiableList(queue);
    }

    /** Get only unread notifications. */
    public List<Notification> getUnread() {
        List<Notification> unread = new ArrayList<>();
        for (Notification n : queue)
            if (!n.isRead()) unread.add(n);
        return Collections.unmodifiableList(unread);
    }

    /** Get notifications filtered by type. */
    public List<Notification> getByType(NotificationType type) {
        if (type == null) return new ArrayList<>();
        List<Notification> result = new ArrayList<>();
        for (Notification n : queue)
            if (n.getType() == type) result.add(n);
        return Collections.unmodifiableList(result);
    }

    /** Get notifications related to a specific email. */
    public List<Notification> getByEmail(String email) {
        if (email == null || email.trim().isEmpty()) return new ArrayList<>();
        List<Notification> result = new ArrayList<>();
        for (Notification n : queue)
            if (n.getRelatedEmail().equalsIgnoreCase(email.trim()))
                result.add(n);
        return Collections.unmodifiableList(result);
    }

    // ─── Mark Read ────────────────────────────────────────────

    /** Mark a single notification as read by its ID. */
    public void markRead(int notifId) {
        for (Notification n : queue)
            if (n.getNotifId() == notifId) { n.markAsRead(); return; }
    }

    /** Mark ALL notifications as read. */
    public void markAllRead() {
        for (Notification n : queue)
            if (!n.isRead()) n.markAsRead();
    }

    // ─── Counts ───────────────────────────────────────────────

    /** Get count of unread notifications. */
    public int getUnreadCount() {
        int count = 0;
        for (Notification n : queue)
            if (!n.isRead()) count++;
        return count;
    }

    /** Get total notification count. */
    public int getTotalCount() { return queue.size(); }

    // ─── Find ─────────────────────────────────────────────────

    /** Find a notification by its ID. Returns null if not found. */
    public Notification findById(int notifId) {
        for (Notification n : queue)
            if (n.getNotifId() == notifId) return n;
        return null;
    }

    // ─── Summary ──────────────────────────────────────────────

    /** Print a formatted summary of all notifications to console. */
    public void printSummary() {
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║         ADMIN NOTIFICATION CENTER            ║");
        System.out.println("╠══════════════════════════════════════════════╣");
        System.out.printf( "  Total: %d | Unread: %d%n",
                getTotalCount(), getUnreadCount());
        System.out.println("──────────────────────────────────────────────");
        for (Notification n : queue)
            System.out.println("  " + n.getDisplayString());
        System.out.println("╚══════════════════════════════════════════════╝");
    }

    // ─── Load from DB ─────────────────────────────────────────

    /** Load saved notifications from DB on startup. */
    public void loadNotifications(List<Notification> saved) {
        if (saved != null) queue.addAll(saved);
    }
}
