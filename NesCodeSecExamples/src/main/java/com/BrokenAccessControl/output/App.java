<|editable_region_start|>
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

    public String getPwd(String email) {
        log.info("Get password");
        CustomerDTO customer = new CustomerDTO(loanRepository.findByEmail(email));
        return customer.getPassword();
    }
}
<|editable_region_end|>
```
