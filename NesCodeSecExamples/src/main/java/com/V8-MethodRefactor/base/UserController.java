// Controller Class - UserController.java
@RestController
public class UserController {
    
    // Error: Missing injection
    private UserService userService;
    
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }
    
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        // Error: No exception handling
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
    
    public ResponseEntity<User> createUser(@RequestBody UserDto userDto) {
        // Error: Missing @Valid annotation
        User user = userService.createUser(userDto);
        return ResponseEntity.ok(user);
    }

    public ResponseEntity<User> updateUser(@PathVariable Long id, @Valid @RequestBody UserDto userDto) {
        User user = userService.updateUser(id, userDto);
        return ResponseEntity.ok(user);
    }
    
    public void deleteUser(@PathVariable Long id) {
        // Error: Should return ResponseEntity
        userService.deleteUser(id);
    }
    
    // Error: Missing @PostMapping
    public ResponseEntity<String> changePassword(@PathVariable Long id, @RequestBody String newPassword) {
        userService.changeUserPassword(id, newPassword);
        return ResponseEntity.ok("Password changed successfully");
    }

    public Admin getAdmin(String username, String password) {
        Admin admin = userService.adminlogin(username, password);
        return admin;
    }
}
