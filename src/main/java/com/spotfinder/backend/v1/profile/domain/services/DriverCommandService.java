package com.spotfinder.backend.v1.profile.domain.services;





import com.spotfinder.backend.v1.profile.domain.model.aggregates.Driver;
import com.spotfinder.backend.v1.profile.domain.model.commands.CreateDriverCommand;

import java.util.Optional;

public interface DriverCommandService {
    Optional<Driver> handle(CreateDriverCommand command, Long userId);
}
