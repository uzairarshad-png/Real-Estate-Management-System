package interfaces;

import notification.Notification;
import java.util.List;

/**
 * Defines notification-receiving behaviour.
 * Implemented by: Admin
 */
public interface Notifiable {

    /**
     * Receive and store an incoming notification.
     * @param notification the notification object to be delivered
     */
    void sendNotification(Notification notification);

    /**
     * Retrieve all notifications received by this entity.
     * @return list of all notifications (read and unread)
     */
    List<Notification> getNotifications();
}
