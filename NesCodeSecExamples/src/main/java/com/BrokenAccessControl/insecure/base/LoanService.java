```<|start_of_file|>
<|editable_region_start|>
package org.mhh.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mhh.Repository.LoanRepository;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@EnableMethodSecurity(prePostEnabled = true)
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
