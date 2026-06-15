package com.gregory.vehicleRentalAPI.customer;

import com.gregory.vehicleRentalAPI.customer.dto.CustomerRequest;
import com.gregory.vehicleRentalAPI.customer.dto.CustomerResponse;
import com.gregory.vehicleRentalAPI.vehicle.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerService {
    CustomerResponse createCustomer(CustomerRequest request, String createdByEmail);
    CustomerResponse getCustomerById(Long id);
    Page<CustomerResponse> getAllCustomers(Pageable pageable);
    CustomerResponse updateCustomer(Long id, CustomerRequest request);
    void deleteCustomer(Long id);
    Customer findById(Long id);
}