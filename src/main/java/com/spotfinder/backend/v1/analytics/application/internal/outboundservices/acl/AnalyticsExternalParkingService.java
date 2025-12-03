package com.spotfinder.backend.v1.analytics.application.internal.outboundservices.acl;

import com.spotfinder.backend.v1.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.spotfinder.backend.v1.parkingManagement.domain.model.aggregates.Parking;
import com.spotfinder.backend.v1.parkingManagement.domain.model.valueobjects.OwnerId;
import com.spotfinder.backend.v1.parkingManagement.infrastructure.persistence.jpa.repositories.ParkingRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Analytics ACL facade to access parking data.
 * Renamed to avoid bean name collision with Reservations module.
 */
@Component
public class AnalyticsExternalParkingService {
    private final ParkingRepository parkingRepository;
    private final UserRepository userRepository;

    public AnalyticsExternalParkingService(ParkingRepository parkingRepository, UserRepository userRepository) {
        this.parkingRepository = parkingRepository;
        this.userRepository = userRepository;
    }

    public List<Parking> findAll() { 
        // Obtener usuario actual del contexto de seguridad
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        
        System.out.println("🔍 [AnalyticsParkingService] Auth: " + (auth != null ? auth.getName() : "null"));
        
        if (auth == null) {
            System.err.println("❌ [AnalyticsParkingService] No authentication found");
            return List.of();
        }
        
        try {
            String principalName = auth.getName();
            Long userId = null;

            System.out.println("📝 [AnalyticsParkingService] Original auth.getName(): " + principalName);

            // Lógica inteligente: ¿Es número o es email?
            if (principalName.matches("\\d+")) {
                // Si son solo números (ej: "19"), lo usamos directo
                userId = Long.parseLong(principalName);
                System.out.println("✅ [AnalyticsParkingService] Using numeric userId directly: " + userId);
            } else {
                // Si es email, buscamos el usuario en la BD para sacar su ID
                var userOptional = userRepository.findByEmail(principalName);

                if (userOptional.isPresent()) {
                    userId = userOptional.get().getId();
                    System.out.println("✅ [AnalyticsParkingService] User found by email. ID: " + userId);
                } else {
                    System.err.println("❌ [AnalyticsParkingService] User not found with email: " + principalName);
                    return List.of();
                }
            }
            
            var ownerId = new com.spotfinder.backend.v1.parkingManagement.domain.model.valueobjects.OwnerId(userId);
            
            System.out.println("🔎 [AnalyticsParkingService] Searching parkings for userId: " + userId);
            
            List<Parking> parkings = parkingRepository.findParkingsByOwnerId(ownerId);
            
            System.out.println("✅ [AnalyticsParkingService] Found " + parkings.size() + " parkings");
            
            return parkings;
        } catch (NumberFormatException e) {
            System.err.println("❌ [AnalyticsParkingService] NumberFormatException: " + e.getMessage());
            return List.of();
        } catch (Exception e) {
            System.err.println("❌ [AnalyticsParkingService] Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    public List<Parking> findById(Long parkingId) {
        if (parkingId == null) return List.of();
        
        // Obtener usuario actual del contexto de seguridad
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return List.of();
        
        try {
            String principalName = auth.getName();
            Long userId = null;

            // Lógica inteligente: ¿Es número o es email?
            if (principalName.matches("\\d+")) {
                userId = Long.parseLong(principalName);
            } else {
                var userOptional = userRepository.findByEmail(principalName);
                if (userOptional.isPresent()) {
                    userId = userOptional.get().getId();
                } else {
                    return List.of();
                }
            }

            var ownerId = new com.spotfinder.backend.v1.parkingManagement.domain.model.valueobjects.OwnerId(userId);
            
            // Obtener el parking y verificar que pertenezca al usuario actual
            var parking = parkingRepository.findById(parkingId);
            return parking
                    .filter(p -> p.getOwnerId() != null && p.getOwnerId().equals(ownerId))
                    .map(List::of)
                    .orElse(List.of());
        } catch (NumberFormatException e) {
            return List.of();
        }
    }
}
