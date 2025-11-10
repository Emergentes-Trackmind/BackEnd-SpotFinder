package com.spotfinder.backend.v1.parkingManagement.domain.model.validators;

import com.spotfinder.backend.v1.parkingManagement.domain.model.aggregates.Parking;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ParkingValidator {
    public record ValidationError(String field, String message) {}

    public List<ValidationError> validate(Parking parking) {
        List<ValidationError> errors = new ArrayList<>();

        if (parking.getName() == null || parking.getName().trim().isEmpty()) {
            errors.add(new ValidationError("name", "El nombre del parking es requerido"));
        }

        if (parking.getOwnerId() == null) {
            errors.add(new ValidationError("ownerId", "El ID del propietario es requerido"));
        }

        if (parking.getTotalSpots() != null && parking.getTotalSpots() < 0) {
            errors.add(new ValidationError("totalSpots", "El número total de spots no puede ser negativo"));
        }

        if (parking.getAvailableSpots() != null) {
            if (parking.getAvailableSpots() < 0) {
                errors.add(new ValidationError("availableSpots", "El número de spots disponibles no puede ser negativo"));
            }
            if (parking.getTotalSpots() != null && parking.getAvailableSpots() > parking.getTotalSpots()) {
                errors.add(new ValidationError("availableSpots", "El número de spots disponibles no puede ser mayor al total"));
            }
        }

        if (parking.getRatePerHour() != null && parking.getRatePerHour() < 0) {
            errors.add(new ValidationError("ratePerHour", "La tarifa por hora no puede ser negativa"));
        }

        return errors;
    }
}