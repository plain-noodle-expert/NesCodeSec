```<|start_of_file|>
<|editable_region_start|>
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findAllByOwnerIdOrderByCreatedOnDesc(UUID userId);

    List<Transaction> findAllBySenderOrReceiverOrderByCreatedOnDesc(String sender, String receiver);

    String getTransactionClient() {
        return this.receiver;
    }

    List<Transaction> findAllByOwnerId(UUID userId) {
        // Implementation here
        <|user_cursor_is_here|>
        return null;
}
<|editable_region_end|>
```

<|recently_viewed_code_snippet|>
package app.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import app.transaction.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Flow.Subscription;
@Configuration
@EnableMethodSecurity
public class WebConfiguration implements WebMvcConfigurer {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        httpSecurity.authorizeHttpRequests(matcher -> matcher
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .requestMatchers("/", "/register", "/error").permitAll()
//                        .requestMatchers("/admin-panel").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(new AntPathRequestMatcher("/api/**"))
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                )
                .formLogin( formLogin -> formLogin
                        .loginPage("/login")
                        .defaultSuccessUrl("/home", true)
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                        .logoutSuccessUrl("/")
                );

        return httpSecurity.build();
    }

}
<|/recently_viewed_code_snippet|>
<|recently_viewed_code_snippet|>
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private User owner;

    @Column(nullable = false)
    private String sender;

    @Column(nullable = false)
    private String receiver;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private BigDecimal balanceLeft;

    @Column(nullable = false)
    private Currency currency;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Column(nullable = false)
    private String description;

    private String failureReason;

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdOn;
}
<|/recently_viewed_code_snippet|>
<|recently_viewed_code_snippet|>
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column
    private String firstName;

    @Column
    private String lastName;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Country country;

    private boolean active;

    @OrderBy("createdOn DESC")
    @OneToMany(fetch = FetchType.EAGER,
               mappedBy = "owner")
    private List<Subscription> subscriptions = new ArrayList<>();

    @OrderBy("createdOn ASC")
    @OneToMany(fetch = FetchType.EAGER,
               mappedBy = "owner")
    private List<Wallet> wallets = new ArrayList<>();
}
<|/recently_viewed_code_snippet|>
<|/recently_viewed_code_snippets|>