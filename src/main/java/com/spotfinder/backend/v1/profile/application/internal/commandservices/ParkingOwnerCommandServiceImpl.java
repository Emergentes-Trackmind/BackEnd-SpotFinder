package com.spotfinder.backend.v1.profile.application.internal.commandservices;


import com.spotfinder.backend.v1.profile.domain.model.aggregates.ParkingOwner;
import com.spotfinder.backend.v1.profile.domain.model.commands.CreateParkingOwnerCommand;
import com.spotfinder.backend.v1.profile.domain.services.ParkingOwnerCommandService;
import com.spotfinder.backend.v1.profile.infrastructure.persistence.jpa.repositories.ProductOwnerRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ParkingOwnerCommandServiceImpl implements ParkingOwnerCommandService {
    private final ProductOwnerRepository distributorRepository;

    public ParkingOwnerCommandServiceImpl(ProductOwnerRepository productOwnerRepository) {
        this.distributorRepository = productOwnerRepository;
    }

    @Override
    public Optional<ParkingOwner> handle(CreateParkingOwnerCommand command, Long userId) {
        if (distributorRepository.existsByPhone_Phone(command.phone())){
            throw new IllegalArgumentException("Phone already exists");
        }

        if (distributorRepository.existsByRuc_Ruc(command.ruc())){
            throw new IllegalArgumentException("Ruc already exists");
        }

        var parkingOwner = new ParkingOwner(command, userId);
        var createdParkingOwner = distributorRepository.save(parkingOwner);
        return Optional.of(createdParkingOwner);
    }
}
