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
        try {
            User user = userService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    @PostMapping("/users")
    public ResponseEntity<User> createUser(@Valid @RequestBody UserDto userDto) {
        try {
            User user = userService.createUser(userDto);
            return ResponseEntity.ok(user);
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @Valid @RequestBody UserDto userDto) {
        try {
            User user = userService.updateUser(id, userDto);
            return ResponseEntity.ok(user);
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok("User deleted successfully");
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Error: Missing @PostMapping
    @PostMapping("/users/{id}/password")
    public ResponseEntity<String> changePassword(@PathVariable Long id, @RequestBody String newPassword) {
        try {
            userService.changeUserPassword(id, newPassword);
            return ResponseEntity.ok("Password changed successfully");
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Error: Should return ResponseEntity
    @PostMapping("/admin/login")
    public ResponseEntity<Admin> adminLogin(@RequestBody AdminLoginDto adminLoginDto) {
        try {
            Admin admin = userService.adminlogin(adminLoginDto.getUsername(), adminLoginDto.getPassword());
            return ResponseEntity.ok(admin);
        } catch (AdminNotFoundException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    
    public Admin getAdmin(String username, String password) {
        Admin admin = userService.adminlogin(username, password);
        return admin;
    }
}
