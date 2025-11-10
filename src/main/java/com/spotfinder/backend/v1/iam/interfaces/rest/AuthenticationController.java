package com.spotfinder.backend.v1.iam.interfaces.rest;

import com.spotfinder.backend.v1.iam.application.internal.outboundservices.hashing.HashingService;
import com.spotfinder.backend.v1.iam.domain.model.aggregates.User;
import com.spotfinder.backend.v1.iam.domain.model.entities.PasswordResetToken;
import com.spotfinder.backend.v1.iam.domain.model.entities.RefreshToken;
import com.spotfinder.backend.v1.iam.domain.model.entities.Role;
import com.spotfinder.backend.v1.iam.domain.model.valueobjects.Roles;
import com.spotfinder.backend.v1.iam.domain.services.UserCommandService;
import com.spotfinder.backend.v1.iam.infrastructure.persistence.jpa.repositories.PasswordResetTokenRepository;
import com.spotfinder.backend.v1.iam.infrastructure.persistence.jpa.repositories.RefreshTokenRepository;
import com.spotfinder.backend.v1.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.spotfinder.backend.v1.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.spotfinder.backend.v1.iam.infrastructure.tokens.jwt.BearerTokenService;
import com.spotfinder.backend.v1.iam.interfaces.rest.resources.*;
import com.spotfinder.backend.v1.iam.interfaces.rest.transform.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

/**
 * AuthenticationController
 * <p>
 *     This controller is responsible for handling authentication requests.
 *     It exposes two endpoints:
 *     <ul>
 *         <li>POST /api/v1/auth/sign-in</li>
 *         <li>POST /api/v1/auth/sign-up</li>
 *     </ul>
 * </p>
 */
@RestController
@RequestMapping(value = {"/api/v1/authentication", "/api/auth"}, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authentication", description = "Authentication Endpoints")
public class AuthenticationController {
    private final UserCommandService userCommandService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final HashingService hashingService;
    private final BearerTokenService tokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public AuthenticationController(UserCommandService userCommandService,
                                    UserRepository userRepository,
                                    RoleRepository roleRepository,
                                    HashingService hashingService,
                                    BearerTokenService tokenService,
                                    RefreshTokenRepository refreshTokenRepository,
                                    PasswordResetTokenRepository passwordResetTokenRepository) {
        this.userCommandService = userCommandService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.hashingService = hashingService;
        this.tokenService = tokenService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    /**
     * Handles the sign-in request.
     * @param signInResource the sign-in request body.
     * @return the authenticated user resource.
     */
    @PostMapping("/sign-in")
    public ResponseEntity<AuthenticatedUserResource> signIn(@RequestBody SignInResource signInResource) {
        var signInCommand = SignInCommandFromResourceAssembler.toCommandFromResource(signInResource);
        var authenticatedUser = userCommandService.handle(signInCommand);
        if (authenticatedUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var authenticatedUserResource = AuthenticatedUserResourceFromEntityAssembler.toResourceFromEntity(authenticatedUser.get().getLeft(), authenticatedUser.get().getRight());
        return ResponseEntity.ok(authenticatedUserResource);
    }

    /**
     * Handles the sign-up request for developers.
     * @param signUpDriverResource the sign-up request body.
     * @return the created user resource.
     */
    @Operation(summary = "Create Driver")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Driver created"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
    })
    @PostMapping("/sign-up/driver")
    public ResponseEntity<UserResource> signUpDriver(@RequestBody SignUpDriverResource signUpDriverResource) {
        var signUpCommand = SignUpDriverCommandFromResourceAssembler.toCommandFromResource(signUpDriverResource);
        var user = userCommandService.handle(signUpCommand);
        if (user.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        var userResource = UserResourceFromEntityAssembler.toResourceFromEntity(user.get());
        return new ResponseEntity<>(userResource, HttpStatus.CREATED);
    }

    /**
     * Handles the sign-up request for enterprises.
     * @param signUpParkingOwnerResource the sign-up request body.
     * @return the created user resource.
     */
    @Operation(summary = "Create Parking Owner")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Parking Owner created"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
    })
    @PostMapping("/sign-up/parking-owner")
    public ResponseEntity<UserResource> signUpParkingOwner(@RequestBody SignUpParkingOwnerResource signUpParkingOwnerResource) {
        var signUpCommand = SignUpParkingOwnerCommandFromResourceAssembler.toCommandFromResource(signUpParkingOwnerResource);
        var user = userCommandService.handle(signUpCommand);
        if (user.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        var userResource = UserResourceFromEntityAssembler.toResourceFromEntity(user.get());
        return new ResponseEntity<>(userResource, HttpStatus.CREATED);
    }

    // =========================
    // Frontend Angular contract
    // Base: /api/auth/*
    // =========================

    public record LoginRequest(String email, String password, Boolean rememberMe) {}
    public record RegisterRequest(String email, String password, String firstName, String lastName, Boolean acceptTerms) {}
    public record UserPayload(String id, String email, String firstName, String lastName, List<String> roles,
                              boolean isEmailVerified, Date createdAt, Date lastLoginAt, String plan) {}
    public record AuthResponse(UserPayload user, String accessToken, String refreshToken, long expiresIn) {}
    public record TokenRefreshRequest(String refreshToken) {}
    public record TokenRefreshResponse(String accessToken, String refreshToken, long expiresIn) {}

    private UserPayload toUserPayload(User user) {
        String email = user.getEmail();
        String[] names = email.split("@")[0].split("[._]");
        String firstName = names.length > 0 ? capitalize(names[0]) : "";
        String lastName = names.length > 1 ? capitalize(names[1]) : "";
        return new UserPayload(
                String.valueOf(user.getId()),
                user.getEmail(),
                firstName,
                lastName,
                user.getSerializedRoles(),
                true,
                user.getCreatedAt(),
                user.getUpdatedAt(),
                "basic"
        );
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0,1).toUpperCase() + s.substring(1).toLowerCase();
    }

    private String generateAccessToken(User user) {
        Long userId = user.getId();
        return tokenService.generateToken(userId, user.getEmail(), user.getSerializedRoles(), true);
    }

    @Operation(summary = "Login", description = "Authenticate user and return AuthResponse { user, accessToken, refreshToken, expiresIn }")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authenticated"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req) {
        var userOpt = userRepository.findByEmail(req.email());
        if (userOpt.isEmpty()) return ResponseEntity.status(401).build();
        var user = userOpt.get();
        if (!hashingService.matches(req.password(), user.getPassword())) return ResponseEntity.status(401).build();

        String accessToken = generateAccessToken(user);
        String refreshToken = UUID.randomUUID().toString();
        long expiresIn = 60L * 60L; // 1h
        refreshTokenRepository.deleteByUserId(user.getId());
        var refreshExpires = Date.from(Instant.now().plusSeconds(30L * 24L * 60L * 60L));
        refreshTokenRepository.save(new RefreshToken(refreshToken, user.getId(), refreshExpires));

        return ResponseEntity.ok(new AuthResponse(toUserPayload(user), accessToken, refreshToken, expiresIn));
    }

    @Operation(summary = "Register", description = "Create account and return AuthResponse { user, accessToken, refreshToken, expiresIn }")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registered"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest req) {
        if (!Boolean.TRUE.equals(req.acceptTerms())) return ResponseEntity.badRequest().build();
        if (userRepository.existsByEmail(req.email())) return ResponseEntity.status(409).build();

        Role role = roleRepository.findByName(Roles.valueOf("ROLE_DRIVER"))
                .orElseThrow(() -> new RuntimeException("Role not found"));
        var user = new User(req.email(), hashingService.encode(req.password()), List.of(role));
        userRepository.save(user);

        String accessToken = generateAccessToken(user);
        String refreshToken = UUID.randomUUID().toString();
        long expiresIn = 60L * 60L;
        refreshTokenRepository.deleteByUserId(user.getId());
        var refreshExpires = Date.from(Instant.now().plusSeconds(30L * 24L * 60L * 60L));
        refreshTokenRepository.save(new RefreshToken(refreshToken, user.getId(), refreshExpires));

        return ResponseEntity.ok(new AuthResponse(toUserPayload(user), accessToken, refreshToken, expiresIn));
    }

    @Operation(summary = "Refresh token", description = "Exchange refresh token and get new { accessToken, refreshToken, expiresIn }")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Refreshed"),
            @ApiResponse(responseCode = "401", description = "Invalid refresh token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refresh(@RequestBody TokenRefreshRequest req) {
        if (req.refreshToken() == null || req.refreshToken().isBlank()) return ResponseEntity.status(401).build();
        var stored = refreshTokenRepository.findByToken(req.refreshToken());
        if (stored.isEmpty() || stored.get().isExpired()) return ResponseEntity.status(401).build();

        var token = stored.get();
        var userOpt = userRepository.findById(token.getUserId());
        if (userOpt.isEmpty()) return ResponseEntity.status(401).build();
        var user = userOpt.get();

        refreshTokenRepository.deleteByUserId(user.getId());
        String newRefresh = UUID.randomUUID().toString();
        var newRefreshExpires = Date.from(Instant.now().plusSeconds(30L * 24L * 60L * 60L));
        refreshTokenRepository.save(new RefreshToken(newRefresh, user.getId(), newRefreshExpires));

        String accessToken = generateAccessToken(user);
        long expiresIn = 60L * 60L;
        return ResponseEntity.ok(new TokenRefreshResponse(accessToken, newRefresh, expiresIn));
    }

    public record ForgotPasswordRequest(String email) {}
    public record GenericMessage(String message) {}
    public record ResetPasswordRequest(String token, String newPassword) {}

    @Operation(summary = "Forgot password", description = "Request a reset token. Always returns 200 to avoid user enumeration.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email processed"),
            @ApiResponse(responseCode = "400", description = "Invalid email")
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<GenericMessage> forgot(@RequestBody ForgotPasswordRequest req) {
        if (req.email() == null || req.email().isBlank()) return ResponseEntity.badRequest().build();
        var userOpt = userRepository.findByEmail(req.email());
        if (userOpt.isPresent()) {
            String token = UUID.randomUUID().toString();
            var expires = Date.from(Instant.now().plusSeconds(15L * 60L));
            passwordResetTokenRepository.save(new PasswordResetToken(token, userOpt.get().getId(), expires, false));
            System.out.printf("[ForgotPassword] Send reset link to %s with token %s (expires %s)%n", req.email(), token, expires);
        }
        return ResponseEntity.ok(new GenericMessage("If the email exists, a reset link has been sent."));
    }

    @Operation(summary = "Reset password", description = "Reset password using a valid reset token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset"),
            @ApiResponse(responseCode = "400", description = "Invalid token or password")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<GenericMessage> reset(@RequestBody ResetPasswordRequest req) {
        if (req.token() == null || req.token().isBlank() || req.newPassword() == null || req.newPassword().isBlank())
            return ResponseEntity.badRequest().build();

        var tokenOpt = passwordResetTokenRepository.findByToken(req.token());
        if (tokenOpt.isEmpty() || tokenOpt.get().isExpired() || tokenOpt.get().isUsed()) {
            return ResponseEntity.status(400).body(new GenericMessage("Invalid or expired token"));
        }
        var reset = tokenOpt.get();
        var userOpt = userRepository.findById(reset.getUserId());
        if (userOpt.isEmpty()) return ResponseEntity.status(400).body(new GenericMessage("Invalid token"));

        var user = userOpt.get();
        user.setPassword(hashingService.encode(req.newPassword()));
        userRepository.save(user);
        reset.setUsed(true);
        passwordResetTokenRepository.save(reset);
        System.out.printf("[ResetPassword] Password updated for user %s%n", user.getEmail());
        return ResponseEntity.ok(new GenericMessage("Password updated successfully"));
    }
}
