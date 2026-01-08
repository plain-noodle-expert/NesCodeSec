<|current_file_content|>
package org.mhh.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mhh.Repository.LoanRepository;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;

    @PostAuthorize("hasRole('ADMIN')")
    public String getLoanDetails(String email) {
        log.info("Get loan details");
        CustomerDTO customer = new CustomerDTO(loanRepository.findByEmail(email));
        return customer.toString();
    }
}
<|/current_file_content|>

<|recently_viewed_code_snippets|>
<|recently_viewed_code_snippet|>
package org.mhh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
<|/recently_viewed_code_snippet|>
<|recently_viewed_code_snippet|>
package org.mhh.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.util.Set;

@Entity
@Table(name = "customer")
@Getter
@Setter
@RequiredArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")//database handel sequence automatically
    @Column(name = "customer_id")
    private long id;
    private String name;
    private String email;
    @Column(name = "mobile_number")
    private String mobileNumber;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String pwd;
    private String role;
    @Column(name = "create_dt")
    private String createSt;
    @JsonIgnore
    @OneToMany(mappedBy = "customer",fetch = FetchType.EAGER)
    private Set<Authority> authorities;
}
<|/recently_viewed_code_snippet|>
<|recently_viewed_code_snippet|>
package org.mhh.Repository;

import org.mhh.Model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanRepository extends JpaRepository<Customer, Long> {
    Customer findByEmail(String email);

}
<|/recently_viewed_code_snippet|>
<|/recently_viewed_code_snippets|>