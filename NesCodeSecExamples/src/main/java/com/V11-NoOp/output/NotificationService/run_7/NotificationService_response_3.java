


### editable_region_start|>
import java.util.ArrayList;
import java.util.List;

public class NotificationService {
    private List<Notification> notifications;

    public NotificationService() {
        this.notifications = new ArrayList<>();
    }

    public void addNotification(Notification notification) {
        notifications.add(notification);
    }

    public void sendNotifications() {
        for (Notification notification : notifications) {
            notification.send();
        }
    }
}

<|editable_region_end|>
```
