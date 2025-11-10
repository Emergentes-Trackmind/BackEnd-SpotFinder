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
        if (auth == null) return List.of();
        
        try {
            // Obtener el userId como String y eliminar cualquier caracter no numérico
            String userIdStr = auth.getName().replaceAll("[^0-9]", "");
            if (userIdStr.isEmpty()) {
                return List.of();
            }
            
            Long userId = Long.parseLong(userIdStr);
            var ownerId = new com.spotfinder.backend.v1.parkingManagement.domain.model.valueobjects.OwnerId(userId);
            return parkingRepository.findParkingsByOwnerId(ownerId);
        } catch (NumberFormatException e) {
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
