package com.spotfinder.backend.v1.profile.domain.services;





import com.spotfinder.backend.v1.profile.domain.model.aggregates.ParkingOwner;
import com.spotfinder.backend.v1.profile.domain.model.commands.CreateParkingOwnerCommand;

import java.util.Optional;

public interface ParkingOwnerCommandService {
    Optional<ParkingOwner> handle(CreateParkingOwnerCommand command, Long userId);
}
