package com.example.demo.Controller;

import com.example.demo.EntityUser.usertable;
import com.example.demo.ServiceUser.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AppController {

    @Autowired
    UserService userser;

    public String front() {
        return "start";
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }

    @GetMapping("/newuser")
    public String newuser(Model model) {
        model.addAttribute("records", new usertable());
        return "newuser";
    }

    @PostMapping("/newuser")
    public String usersave(@ModelAttribute usertable u) {
        userser.newuser(u);
        return "usersave";
    }

    @GetMapping("/newadmin")
    public String newadmin(Model model) {
        model.addAttribute("records", new usertable());
        return "newadmin";
    }

    @PostMapping("/newadmin")
    public String regcomplete() {
        return "regcomplete";
    }

    @GetMapping("/login")
    public String userlogin(Model model) {
        model.addAttribute("records", new usertable());
        return "login";
    }

    @PostMapping("/login")
    public String userlogin(
        @RequestParam String email,
        @RequestParam String password,
        Model model
    ) {
        usertable obj = userser.userlogindata(email, password);
        if (obj != null) {
            model.addAttribute("msg", "");
            return "userdash";
        } else {
            model.addAttribute("msg", "Invalid email and password");
            return "login";
        }
    }
}
