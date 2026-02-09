// Controller Class - UserController.java

import java.util.List;

@RestController
public class UserController {
    
    // Error: Missing injection
    private UserService userService;
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        // Error: No exception handling
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody UserDto userDto) {
        // Error: Missing @Valid annotation
        User user = userService.createUser(userDto);
        return ResponseEntity.ok(user);
    }
     @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @Valid @RequestBody UserDto userDto) {
        User user = userService.updateUser(id, userDto);
        return ResponseEntity.ok(user);
    }
    
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/users/{id}/change-password")
    public ResponseEntity<String> changePassword(@PathVariable Long id, @RequestBody String newPassword) {
        userService.changeUserPassword(id, newPassword);
        return ResponseEntity.ok("Password changed successfully");
    }
    @PostMapping("/admin/login")
    public Admin getAdmin(String username, String password) {
        Admin admin = userService.adminlogin(username, password);
        return admin;
    }
}
