package com.spotfinder.backend.v1.parkingManagement.domain.model.exceptions;

import com.spotfinder.backend.v1.parkingManagement.domain.model.validators.ParkingValidator;
import java.util.List;

public class ParkingValidationException extends RuntimeException {
    private final List<ParkingValidator.ValidationError> errors;

    public ParkingValidationException(List<ParkingValidator.ValidationError> errors) {
        super("Errores de validación en el parking");
        this.errors = errors;
    }

    public List<ParkingValidator.ValidationError> getErrors() {
        return errors;
    }
}