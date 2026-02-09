// Controller Class - UserController.java
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
        // Error: Should return ResponseEntity
        userService.deleteUser(id);
    }
    @PostMapping("/users/{id}/change-password")
    // Error: Missing @PostMapping
    public ResponseEntity<String> changePassword(@PathVariable Long id, @RequestBody String newPassword) {
        userService.changeUserPassword(id, newPassword);
        return ResponseEntity.ok("Password changed successfully");
    }
    @GetMapping("/admin")
    public ResponseEntity<Admin> getAdmin(@RequestParam String username, @RequestParam String password) {
        Admin admin = userService.adminlogin(username, password);
        return ResponseEntity.ok(admin);
    }   
}
