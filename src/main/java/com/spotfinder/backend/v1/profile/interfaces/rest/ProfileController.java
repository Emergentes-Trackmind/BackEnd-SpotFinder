package com.spotfinder.backend.v1.profile.interfaces.rest;


import com.spotfinder.backend.v1.profile.domain.model.queries.GetDriverByUserIdAsyncQuery;
import com.spotfinder.backend.v1.profile.domain.model.queries.GetParkingOwnerByUserIdAsyncQuery;
import com.spotfinder.backend.v1.profile.domain.services.DriverQueryService;
import com.spotfinder.backend.v1.profile.domain.services.ParkingOwnerQueryService;
import com.spotfinder.backend.v1.profile.interfaces.rest.resource.DriverResource;
import com.spotfinder.backend.v1.profile.interfaces.rest.resource.ParkingOwnerResource;
import com.spotfinder.backend.v1.profile.interfaces.rest.transform.DriverResourceFromEntityAssembler;
import com.spotfinder.backend.v1.profile.interfaces.rest.transform.ParkingOwnerResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.spotfinder.backend.v1.iam.infrastructure.tokens.jwt.BearerTokenService;
import com.spotfinder.backend.v1.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.spotfinder.backend.v1.iam.domain.model.aggregates.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping(value = {"/api/profile", "/api/v1/profiles"}, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Profiles", description = "Profiles Management Endpoints")
public class ProfileController {
    private final DriverQueryService agriculturalProducerQueryService;
    private final ParkingOwnerQueryService distributorQueryService;
    private final BearerTokenService tokenService;
    private final UserRepository userRepository;

    public record ProfileJson(Long id, Long userId, String fullName, String email, String phone, String role, String avatarUrl) {}

    private static final Map<Long, ProfileJson> OVERRIDES = new ConcurrentHashMap<>();

    public ProfileController(DriverQueryService agriculturalProducerQueryService,
                             ParkingOwnerQueryService distributorQueryService,
                             BearerTokenService tokenService,
                             UserRepository userRepository) {
        this.agriculturalProducerQueryService = agriculturalProducerQueryService;
        this.distributorQueryService = distributorQueryService;
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }

    private Long currentUserId(HttpServletRequest request) {
        try { var token = tokenService.getBearerTokenFrom(request); return token != null ? Long.valueOf(tokenService.getUserIdFromToken(token)) : null; } catch (Exception e) { return null; }
    }

    private String firstOrNull(User u) {
        try { var m = User.class.getDeclaredMethod("getFirstName"); return String.valueOf(m.invoke(u)); } catch (Exception ignored) { return null; }
    }
    private String lastOrNull(User u) {
        try { var m = User.class.getDeclaredMethod("getLastName"); return String.valueOf(m.invoke(u)); } catch (Exception ignored) { return null; }
    }
    private String roleOrNull(User u) {
        try { var m = User.class.getDeclaredMethod("getSerializedRoles"); @SuppressWarnings("unchecked") var list = (java.util.List<String>) m.invoke(u); return list!=null && !list.isEmpty()? list.get(0): null; } catch (Exception ignored) { return null; }
    }

    private ProfileJson baseProfile(User u) {
        var fullName = String.join(" ", java.util.stream.Stream.of(firstOrNull(u), lastOrNull(u)).filter(s -> s!=null && !s.isBlank()).toList());
        if (fullName.isBlank()) fullName = u.getEmail();
        return new ProfileJson(u.getId(), u.getId(), fullName, u.getEmail(), null, roleOrNull(u), null);
    }

    @GetMapping
    @Operation(summary = "Get my profile")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Profile returned"), @ApiResponse(responseCode = "401", description = "Unauthorized") })
    public ResponseEntity<ProfileJson> getMyProfile(HttpServletRequest request) {
        var uid = currentUserId(request);
        if (uid == null) return ResponseEntity.status(401).build();
        var user = userRepository.findById(uid).orElse(null);
        if (user == null) return ResponseEntity.status(401).build();
        var base = baseProfile(user);
        var override = OVERRIDES.get(uid);
        if (override != null) {
            base = new ProfileJson(base.id(), base.userId(), override.fullName()!=null?override.fullName():base.fullName(), override.email()!=null?override.email():base.email(), override.phone()!=null?override.phone():base.phone(), override.role()!=null?override.role():base.role(), override.avatarUrl()!=null?override.avatarUrl():base.avatarUrl());
        }
        return ResponseEntity.ok(base);
    }

    @PutMapping
    @Operation(summary = "Update my profile")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Profile updated"), @ApiResponse(responseCode = "401", description = "Unauthorized") })
    public ResponseEntity<ProfileJson> updateMyProfile(HttpServletRequest request, @RequestBody ProfileJson body) {
        var uid = currentUserId(request);
        if (uid == null) return ResponseEntity.status(401).build();
        var existing = OVERRIDES.getOrDefault(uid, body);
        var updated = new ProfileJson(uid, uid,
                body.fullName()!=null?body.fullName():existing.fullName(),
                body.email()!=null?body.email():existing.email(),
                body.phone()!=null?body.phone():existing.phone(),
                body.role()!=null?body.role():existing.role(),
                body.avatarUrl()!=null?body.avatarUrl():existing.avatarUrl());
        OVERRIDES.put(uid, updated);
        return ResponseEntity.ok(updated);
    }

    @GetMapping(value = "/driver/{userId}")
    public ResponseEntity<DriverResource> getDriverByUserId(@PathVariable Long userId) {
        var getAgriculturalProducerByUserIdQuery = new GetDriverByUserIdAsyncQuery(userId);
        var agriculturalProducer = agriculturalProducerQueryService.handle(getAgriculturalProducerByUserIdQuery);
        if (agriculturalProducer.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var agriculturalProducerResource = DriverResourceFromEntityAssembler
                .toResourceFromEntity(agriculturalProducer.get());

        return ResponseEntity.ok(agriculturalProducerResource);
    }

    @GetMapping(value = "/parking-owner/{userId}")
    public ResponseEntity<ParkingOwnerResource> getParkingOwnerByUserId(@PathVariable Long userId) {
        var getDistributorByUserIdQuery = new GetParkingOwnerByUserIdAsyncQuery(userId);
        var distributor = distributorQueryService.handle(getDistributorByUserIdQuery);
        if (distributor.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var distributorResource = ParkingOwnerResourceFromEntityAssembler.toResourceFromEntity(distributor.get());

        return ResponseEntity.ok(distributorResource);
    }
}
