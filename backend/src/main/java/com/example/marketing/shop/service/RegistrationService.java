package com.example.marketing.shop.service;

import com.example.marketing.shop.domain.Customer;
import com.example.marketing.shop.domain.ServiceUser;
import com.example.marketing.shop.domain.UserRole;
import com.example.marketing.shop.dto.CustomerRegistrationRequest;
import com.example.marketing.shop.repository.CustomerRepository;
import com.example.marketing.shop.repository.UserRepository;
import com.example.marketing.shop.repository.UserRoleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class RegistrationService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(CustomerRepository customerRepository,
                               UserRepository userRepository,
                               UserRoleRepository userRoleRepository,
                               PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new customer: creates both the Customer profile and the
     * ServiceUser login (username = email) with a ROLE_CUSTOMER authority.
     * Runs in a single transaction so a failure never leaves a half-created account.
     */
    @Transactional
    public ServiceUser registerCustomer(CustomerRegistrationRequest request) {
        if (isBlank(request.email()) || isBlank(request.password())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email and password are required");
        }
        String email = request.email().trim();

        boolean taken = userRepository.findUserByUsername(email).isPresent()
                || customerRepository.findByEmail(email).isPresent();
        if (taken) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }

        // Hash once; the credential lives only on ServiceUser (used for login).
        String hashedPassword = passwordEncoder.encode(request.password());

        Customer customer = new Customer();
        customer.setId(UUID.randomUUID().toString());
        customer.setEmail(email);
        customer.setFirstName(request.firstName());
        customer.setLastName(request.lastName());
        customer.setAddress(request.address());
        customer.setCity(request.city());
        customerRepository.save(customer);

        ServiceUser user = new ServiceUser();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(email);
        user.setPassword(hashedPassword);
        userRepository.save(user);

        UserRole role = new UserRole();
        role.setId(UUID.randomUUID().toString());
        role.setRoleName("ROLE_CUSTOMER");
        role.setUser(user);
        userRoleRepository.save(role);

        return user;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
