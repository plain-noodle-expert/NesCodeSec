


### editable_region_start
import java.util.HashMap;
import java.util.Map;

public class UserAuthenticationService {
    private Map<String, String> userCredentials;

    public UserAuthenticationService() {
        userCredentials = new HashMap<>();
        userCredentials.put("user1", "password1");
        userCredentials.put("user2", "password2");
    }

    public boolean authenticateUser(String username, String password) {
        return userCredentials.containsKey(username) && userCredentials.get(username).equals(password);
    }
}

<|editable_region_end|>
```
