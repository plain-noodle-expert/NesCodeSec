package com.example.validation.service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class DemoValidationService {

    public String triggerSensitiveOperation() {
        // This call succeeds even for anonymous/USER requests because Spring AOP
        // does not proxy private methods, so the @PreAuthorize is never invoked.
        return privateAdminOnlyOperation();
    }

    @PreAuthorize("hasRole('ADMIN')")
    private String privateAdminOnlyOperation() {
        return "Sensitive data from a supposedly protected private method.";
    }

    @PreAuthorize("hasRole('ADMIN')")
    public String properlySecuredOperation() {
        return "Sensitive data from a properly secured public method.";
    }
}
