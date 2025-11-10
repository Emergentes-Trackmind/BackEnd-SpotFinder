package com.spotfinder.backend.v1.notifications.interfaces.rest;

import com.spotfinder.backend.v1.notifications.domain.model.FcmToken;
import com.spotfinder.backend.v1.notifications.domain.repository.FcmTokenRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "Notification Management Endpoints")
public class TokenController {

    private final FcmTokenRepository fcmTokenRepository;

    public TokenController(FcmTokenRepository fcmTokenRepository) {
        this.fcmTokenRepository = fcmTokenRepository;
    }

    @Operation(
            summary = "Register a notification token for a user",
            description = "Registers a new FCM token or updates its associated user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Token registered or updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters provided")
    })
    @PostMapping("/register-token")
    public ResponseEntity<Void> registerToken(@RequestParam Long userId, @RequestParam String token) {
        fcmTokenRepository.findByToken(token).ifPresentOrElse(existing -> {
            if (!existing.getUserId().equals(userId)) {
                existing.setUserId(userId);
                fcmTokenRepository.save(existing);
            }
        }, () -> {
            FcmToken newToken = new FcmToken();
            newToken.setUserId(userId);
            newToken.setToken(token);
            fcmTokenRepository.save(newToken);
        });

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(
            summary = "Unregister a notification token",
            description = "Removes an FCM token from the system."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token unregistered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid token parameter provided")
    })
    @DeleteMapping("/unregister-token")
    public ResponseEntity<Void> unregisterToken(@RequestParam String token) {
        fcmTokenRepository.deleteByToken(token);
        return ResponseEntity.ok().build();
    }
}
