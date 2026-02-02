


### editable_region_start
import java.util.ArrayList;
import java.util.List;

public class NotificationService {
    private List<NotificationListener> listeners = new ArrayList<>();

    public void addListener(NotificationListener listener) {
        listeners.add(listener);
    }

    public void removeListener(NotificationListener listener) {
        listeners.remove(listener);
    }

    public void notifyListeners(String message) {
        for (NotificationListener listener : listeners) {
            listener.onNotification(message);
        }
    }
}

<|editable_region_end|>
```
