package com.example.validation.controller;

import com.example.validation.service.DemoValidationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DemoController {

    private final DemoValidationService demoValidationService;

    public DemoController(DemoValidationService demoValidationService) {
        this.demoValidationService = demoValidationService;
    }

    @GetMapping("/public")
    public ResponseEntity<String> hitPublicEndpoint(Authentication authentication) {
        String caller = authentication != null ? authentication.getName() : "anonymous";
        String result = demoValidationService.triggerSensitiveOperation();
        return ResponseEntity.ok("Caller: " + caller + " | Result: " + result);
    }

    @GetMapping("/protected")
    public ResponseEntity<String> hitProtectedEndpoint() {
        return ResponseEntity.ok(demoValidationService.properlySecuredOperation());
    }
}
