package com.example.marketing.shop.controller;

import com.example.marketing.shop.domain.ServiceUser;
import com.example.marketing.shop.dto.CustomerRegistrationRequest;
import com.example.marketing.shop.dto.RegistrationResponse;
import com.example.marketing.shop.service.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/register/customers")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody CustomerRegistrationRequest request) {
        ServiceUser user = registrationService.registerCustomer(request);
        RegistrationResponse body = new RegistrationResponse(
                user.getId(), user.getUsername(), "Customer registered successfully");
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }
}
