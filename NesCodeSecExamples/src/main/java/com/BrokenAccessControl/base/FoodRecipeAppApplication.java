<|current_file_content|>
package com.uas.kelompoksatu.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.uas.kelompoksatu.user.Entities.User;
import com.uas.kelompoksatu.user.Entities.UserRole;

import jakarta.transaction.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public User registerUser(String username, String password, UserRole role) {
        if (userRepository.findByUsername(username) != null) {
            throw new RuntimeException("User already exists");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(bCryptPasswordEncoder.encode(password));
        user.setRole(role);
        user.setLoggedIn(false);
        return userRepository.save(user);
    }

    @Transactional
    public User loginUser(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user == null || !bCryptPasswordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }
        if (user.isLoggedIn()) {
            throw new RuntimeException("User already logged in");
        }
        user.setLoggedIn(true);
        return userRepository.save(user);
    }

    @Transactional
    public void logoutUser(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null && user.isLoggedIn()) {
            user.setLoggedIn(false);
            userRepository.save(user);
        }
    }
}
<|/current_file_content|>

<|recently_viewed_code_snippets|>
<|recently_viewed_code_snippet|>
package com.uas.kelompoksatu.user.Entities;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String username;
    private String password;
    private Boolean loggedIn;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    public boolean isLoggedIn() {
        return loggedIn;
    }

}
<|/recently_viewed_code_snippet|>
<|recently_viewed_code_snippet|>
package com.uas.kelompoksatu.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.uas.kelompoksatu.user.Entities.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    User findByUsername(String username);
}
<|/recently_viewed_code_snippet|>
<|recently_viewed_code_snippet|>
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfiguration {

    @Autowired
    private UserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // Configure authentication manager here
        auth
                .inMemoryAuthentication()
                .withUser("user")
                .password("{noop}password")
                .roles("USER");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                .antMatchers("/api/users/register").permitAll()
                .antMatchers("/api/users/login").permitAll()
                .antMatchers("/api/users/logout").authenticated()
                .antMatchers("/api/users/upgrade").hasAnyAuthority("USER", "PREMIUM_USER")
                .antMatchers("/admin/**").hasAuthority("ADMIN")
                .anyRequest().authenticated()
                .and()
                .formLogin().disable()
                .sessionManagement().maximumSessions(1);
    }

}
<|/recently_viewed_code_snippet|>
<|recently_viewed_code_snippet|>
package com.uas.kelompoksatu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class FoodRecipeAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(FoodRecipeAppApplication.class, args);
	}

}
<|/recently_viewed_code_snippet|>
<|/recently_viewed_code_snippets|>