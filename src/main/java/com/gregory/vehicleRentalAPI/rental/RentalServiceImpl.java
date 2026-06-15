package com.gregory.vehicleRentalAPI.rental;

import com.gregory.vehicleRentalAPI.customer.Customer;
import com.gregory.vehicleRentalAPI.customer.CustomerService;
import com.gregory.vehicleRentalAPI.payment.PaymentService;
import com.gregory.vehicleRentalAPI.payment.dto.PaymentResponse;
import com.gregory.vehicleRentalAPI.rental.dto.RentalRequest;
import com.gregory.vehicleRentalAPI.rental.dto.RentalResponse;
import com.gregory.vehicleRentalAPI.shared.events.RentalCancelledEvent;
import com.gregory.vehicleRentalAPI.shared.exception.BusinessRuleException;
import com.gregory.vehicleRentalAPI.shared.exception.ResourceNotFoundException;
import com.gregory.vehicleRentalAPI.user.User;
import com.gregory.vehicleRentalAPI.user.UserService;
import com.gregory.vehicleRentalAPI.vehicle.Vehicle;
import com.gregory.vehicleRentalAPI.vehicle.VehicleService;
import com.gregory.vehicleRentalAPI.vehicle.VehicleStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service @RequiredArgsConstructor
public class RentalServiceImpl implements RentalService {

    private final VehicleService vehicleService;
    private final RentalRepository rentalRepository;
    private final UserService userService;
    private final CustomerService customerService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional @Override
    public RentalResponse createRental(RentalRequest request, String createdByEmail){

        Vehicle vehicle = vehicleService.findById(request.vehicleId());
        Customer customer = customerService.findById(request.customerId());

        if (vehicle.getStatus() != VehicleStatus.AVAILABLE)
            throw new BusinessRuleException("This vehicle is not available");

        if (!customer.isActive())
            throw new BusinessRuleException("This customer is not active");

        long days = ChronoUnit.DAYS.between(request.startDate(), request.expectedReturnDate());

        if (days < 1)
            throw new BusinessRuleException("The minimum rental is 1 day");


        BigDecimal expectedAmount = BigDecimal.valueOf(days).multiply(vehicle.getDailyRate());

        User employee = userService.findByEmail(createdByEmail);

        Rental rental = Rental.builder()
                .createdBy(employee)
                .customer(customer)
                .vehicle(vehicle)
                .startDate(request.startDate())
                .expectedReturnDate(request.expectedReturnDate())
                .expectedAmount(expectedAmount)
                .dailyRateSnapshot(vehicle.getDailyRate())
                .status(RentalStatus.ACTIVE)
                .notes(request.notes())
                .build();

        vehicle.setStatus(VehicleStatus.RENTED);
        return RentalResponse.from(rentalRepository.save(rental));
    }

    @Transactional(readOnly = true) @Override
    public RentalResponse getRentalById(Long id){
        Rental rental = rentalRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Rental with id " + id + " not found"));

        return RentalResponse.from(rental);
    }

    @Transactional(readOnly = true) @Override
    public Page<RentalResponse> getAllRentals(RentalStatus status, Pageable pageable){
        return rentalRepository.findByStatus(status, pageable)
                .map(RentalResponse::from);
    }

    @Transactional @Override
    public RentalResponse returnVehicle(Long id, Integer mileage){
        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not exists rental with id: " + id));

        if (rental.getStatus() != RentalStatus.ACTIVE && rental.getStatus() != RentalStatus.OVERDUE)
            throw new BusinessRuleException("Rental with id " +id+" is not active");

        BigDecimal paid = (rental.getTotalAmount() == null) ? BigDecimal.ZERO : rental.getTotalAmount();

        if (paid.compareTo(rental.getExpectedAmount()) < 0)
            throw new BusinessRuleException("The total amount ("+paid+") is less than the expected amount ("+
                    rental.getExpectedAmount()+")");

        Vehicle vehicle = vehicleService.findById(rental.getVehicle().getId());
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        vehicle.setMileage(mileage);
        rental.setActualReturnDate(LocalDateTime.now());
        rental.setStatus(RentalStatus.COMPLETED);

        return RentalResponse.from(rentalRepository.save(rental));
    }

    @Transactional @Override
    public RentalResponse cancelRental(Long id){
        Rental rental = rentalRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Not exists rental with id: " + id));

        if (rental.getStatus() != RentalStatus.ACTIVE && rental.getStatus() != RentalStatus.OVERDUE)
            throw new BusinessRuleException("You can only cancel an active rental");

        Vehicle vehicle = vehicleService.findById(rental.getVehicle().getId());
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        rental.setStatus(RentalStatus.CANCELLED);
        rentalRepository.save(rental);
        applicationEventPublisher.publishEvent(new RentalCancelledEvent(rental.getId()));
        return RentalResponse.from(rental);
    }

    @Transactional(readOnly = true) @Override
    public Rental findById(Long id) {
        return rentalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Rental with id " + id + " not found"));
    }

    @Transactional @Override
    public void updateTotalAmount(Long id, BigDecimal totalAmount){
        Rental rental = rentalRepository.findById(id)
                .orElseThrow((()-> new ResourceNotFoundException("Rental with id "+id+" not found")));

        rental.setTotalAmount(totalAmount);
    }
}
