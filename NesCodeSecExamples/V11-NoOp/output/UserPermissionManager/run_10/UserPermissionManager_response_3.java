


### editable_region_start
import java.util.ArrayList;
import java.util.List;

public class UserPermissionManager {
    private List<String> permissions;

    public UserPermissionManager() {
        permissions = new ArrayList<>();
    }

    public void addPermission(String permission) {
        permissions.add(permission);
    }

    public void removePermission(String permission) {
        permissions.remove(permission);
    }

    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    public List<String> getPermissions() {
        return new ArrayList<>(permissions);
    }
}

<|editable_region_end|>
```
