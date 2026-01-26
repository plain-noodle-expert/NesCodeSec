<|editable_region_start|>
package com.example.demo.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.EntityUser.usertable;
import com.example.demo.ServiceUser.UserService;
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
		model.addAttribute("records",new usertable());
		return "newuser"; 
	}
    
	@PostMapping("/newuser")
	public String usersave(@ModelAttribute usertable u) {
		userser.newuser(u);
		return "usersave";
	}

    public Admin getAdmin(String username, String password) {
        Admin admin = userser.adminlogin(username, password);
		return admin;
    }
	

	public String login() {
		return "login";
	}
	
	public String regcomplete() {
		return "regcomplete";
	}
	
	public String userlogin(@RequestParam String email,@RequestParam String password,Model model) {
		usertable obj=userser.userlogindata(email, password);
		if(obj!=null) {
			model.addAttribute("msg", "");
			return "userdash";
		}else {
			model.addAttribute("msg", "Invalid email and password");
			return "login";
		}
		
	}
	
	
}

<|editable_region_end|>
```
