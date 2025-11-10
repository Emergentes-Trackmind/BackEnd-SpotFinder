package com.spotfinder.backend.v1.iam.application.internal.commandservices;


import com.spotfinder.backend.v1.iam.application.internal.outboundservices.acl.ExternalProfileService;
import com.spotfinder.backend.v1.iam.application.internal.outboundservices.hashing.HashingService;
import com.spotfinder.backend.v1.iam.infrastructure.tokens.jwt.BearerTokenService;
import com.spotfinder.backend.v1.iam.domain.model.aggregates.User;
import com.spotfinder.backend.v1.iam.domain.model.commands.SignInCommand;
import com.spotfinder.backend.v1.iam.domain.model.commands.SignUpDriverCommand;
import com.spotfinder.backend.v1.iam.domain.model.commands.SignUpParkingOwnerCommand;
import com.spotfinder.backend.v1.iam.domain.model.entities.Role;
import com.spotfinder.backend.v1.iam.domain.model.valueobjects.Roles;
import com.spotfinder.backend.v1.iam.domain.services.UserCommandService;
import com.spotfinder.backend.v1.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.spotfinder.backend.v1.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserCommandServiceImpl implements UserCommandService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final HashingService hashingService;
    private final BearerTokenService tokenService;
    private final ExternalProfileService externalProfileService;

    public UserCommandServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
                                  HashingService hashingService, BearerTokenService tokenService,
                                  ExternalProfileService externalProfileService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.hashingService = hashingService;
        this.tokenService = tokenService;
        this.externalProfileService = externalProfileService;
    }

    @Override
    public Optional<ImmutablePair<User, String>> handle(SignInCommand command) {
        var user = userRepository.findByEmail(command.email());
        if (user.isEmpty())
            throw new RuntimeException("User not found");
        if (!hashingService.matches(command.password(), user.get().getPassword()))
            throw new RuntimeException("Invalid password");
        // Generate token with numeric subject (user id) and email claim
        var token = tokenService.generateToken(user.get().getId(), user.get().getEmail(), user.get().getSerializedRoles(), true);
        return Optional.of(ImmutablePair.of(user.get(), token));
    }

    @Override
    @Transactional
    public Optional<User> handle(SignUpDriverCommand command) {
        if (userRepository.existsByEmail(command.email()))
            throw new RuntimeException("Email already exists");

        // Buscar el rol de driver en el repositorio de roles
        Role driverRole = roleRepository.findByName(Roles.valueOf("ROLE_DRIVER"))
                .orElseThrow(() -> new RuntimeException("Driver role not found"));

        // Crear una lista con el rol de driver
        List<Role> roles = List.of(driverRole);

        // Crear el usuario con el rol de driver
        var user = new User(command.email(), hashingService.encode(command.password()), roles);
        userRepository.save(user);

        Long driverId = externalProfileService.createDriver(
                command.fullName(), command.city(), command.country(),
                command.phone(), command.dni(), user.getId()
        );

        if (driverId == 0L) {
            throw new RuntimeException("Failed to create Driver profile");
        }

        return Optional.of(user);
    }

    @Override
    @Transactional
    public Optional<User> handle(SignUpParkingOwnerCommand command) {
        if (userRepository.existsByEmail(command.email()))
            throw new RuntimeException("Email already exists");

        // Buscar el rol de Parking Owner en el repositorio de roles
        Role parkingOwnerRole = roleRepository.findByName(Roles.valueOf("ROLE_PARKING_OWNER"))
                .orElseThrow(() -> new RuntimeException("Parking Owner role not found"));

        // Crear una lista con el rol de parking owner
        List<Role> roles = List.of(parkingOwnerRole);

        // Crear el usuario con el rol de parking owner
        var user = new User(command.email(), hashingService.encode(command.password()), roles);
        userRepository.save(user);

        Long parkingOwnerId = externalProfileService.createParkingOwner(
                command.fullName(), command.city(), command.country(),
                command.phone(), command.companyName(), command.ruc(), user.getId()
        );

        if (parkingOwnerId == 0L) {
            throw new RuntimeException("Failed to create Parking Owner profile");
        }

        return Optional.of(user);
    }
}
