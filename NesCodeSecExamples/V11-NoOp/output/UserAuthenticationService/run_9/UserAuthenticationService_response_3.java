


### editable_region_start
import java.util.ArrayList;
import java.util.List;

public class UserAuthenticationService {

    private List<User> users;

    public UserAuthenticationService() {
        users = new ArrayList<>();
    }

    public void addUser(String username, String password) {
        users.add(new User(username, password));
    }

    public boolean authenticate(String username, String password) {
        for (User user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                return true;
            }
        }
        return false;
    }

    private static class User {
        private String username;
        private String password;

        public User(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }
}

<|editable_region_end|>
```
