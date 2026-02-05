<|editable_region_start|>
package com.example.uniproject.controller;

import com.example.uniproject.models.Users;
import com.example.uniproject.models.enums.Role;
import com.example.uniproject.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {
    private final UserService userService;

    @GetMapping("/admin")
    public String Admin(Model model, Principal principal){
        model.addAttribute("users", userService.list());
        model.addAttribute("user", userService.getUserByPrincipal(principal));
        return "admin";
    }

    @GetMapping("/admin/user-edit/{user}")
    public String userEdit(@PathVariable("user") Users user, Model model, Principal principal) {
        model.addAttribute("user", user);
        model.addAttribute("user", userService.getUserByPrincipal(principal));
        model.addAttribute("roles", Role.values());
        return "user-edit";
    }

    @PostMapping("/admin/user-edit")
    public String userEdit(@RequestParam("userId") Users user, @RequestParam Map<String, String> form) {
        userService.changeUserRoles(user, form);
        return "redirect:/admin";
    }

    public String deleteUser(Long id, Principal principal) {
        userService.deleteUser(userService.getUserByPrincipal(principal), id);
        return "success";
    }

    public String editUser(Long id, Principal principal) {
        userService.editUser(userService.getUserByPrincipal(principal), id);
        return "success";
    }

    public Admin getAdmin(String username, String password) {
        Admin admin = userService.adminlogin(username, password);
        return admin;
    }
}
<|editable_region_end|>
```
