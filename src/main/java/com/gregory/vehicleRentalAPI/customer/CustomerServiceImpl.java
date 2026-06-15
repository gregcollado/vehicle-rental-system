package com.gregory.vehicleRentalAPI.customer;

import com.gregory.vehicleRentalAPI.customer.dto.CustomerRequest;
import com.gregory.vehicleRentalAPI.customer.dto.CustomerResponse;
import com.gregory.vehicleRentalAPI.shared.exception.ResourceAlreadyExistsException;
import com.gregory.vehicleRentalAPI.shared.exception.ResourceNotFoundException;
import com.gregory.vehicleRentalAPI.user.User;
import com.gregory.vehicleRentalAPI.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final UserService userService;

    @Override
    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request, String createdByEmail) {

        Optional<Customer> existing = customerRepository.findByLicenseNumber(request.licenseNumber());

        if (existing.isPresent()) {
            Customer customer = existing.get();
            if (customer.isActive()) {
                throw new ResourceAlreadyExistsException(
                        "A customer with license number " + request.licenseNumber() + " already exists");
            }
            // Reactiva y actualiza
            customer.setFirstName(request.firstName());
            customer.setLastName(request.lastName());
            customer.setPhone(request.phone());
            customer.setEmail(request.email());
            customer.setAddress(request.address());
            customer.setActive(true);
            return CustomerResponse.from(customer);
        }

        User employee = userService.findByEmail(createdByEmail);

        Customer customer = Customer.builder()
                .createdBy(employee)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .licenseNumber(request.licenseNumber())
                .phone(request.phone())
                .email(request.email())
                .address(request.address())
                .active(true)
                .build();

        return CustomerResponse.from(customerRepository.save(customer));
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer with id " + id + " not found"));

        return CustomerResponse.from(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerResponse> getAllCustomers(Pageable pageable) {
        return customerRepository.findAllByActiveTrue(pageable)
                .map(CustomerResponse::from);
    }

    @Override
    @Transactional
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer with id " + id + " not found"));

        if (customerRepository.existsByLicenseNumberAndIdNot(request.licenseNumber(), id)) {
            throw new ResourceAlreadyExistsException(
                    "A customer with license number " + request.licenseNumber() + " already exists");
        }

        customer.setFirstName(request.firstName());
        customer.setLastName(request.lastName());
        customer.setLicenseNumber(request.licenseNumber());
        customer.setPhone(request.phone());
        customer.setEmail(request.email());
        customer.setAddress(request.address());

        return CustomerResponse.from(customer);
    }

    @Override
    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer with id " + id + " not found"));

        customer.setActive(false);
    }

    @Transactional(readOnly = true)
    public Customer findById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer with id " + id + " not found"));
    }
}