package com.spotfinder.backend.v1.analytics.application.internal.outboundservices.acl;

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

    public AnalyticsExternalParkingService(ParkingRepository parkingRepository) {
        this.parkingRepository = parkingRepository;
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
            // Obtener el userId como String y eliminar cualquier caracter no numérico
            String originalName = auth.getName();
            String userIdStr = originalName.replaceAll("[^0-9]", "");
            
            System.out.println("📝 [AnalyticsParkingService] Original auth.getName(): " + originalName);
            System.out.println("📝 [AnalyticsParkingService] Extracted userId: " + userIdStr);
            
            if (userIdStr.isEmpty()) {
                System.err.println("❌ [AnalyticsParkingService] No numeric userId found in auth.getName()");
                return List.of();
            }
            
            Long userId = Long.parseLong(userIdStr);
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
            Long userId = Long.parseLong(auth.getName());
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
