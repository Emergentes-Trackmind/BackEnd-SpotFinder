package com.spotfinder.backend.v1.profile.domain.model.aggregates;



import com.spotfinder.backend.v1.profile.domain.model.commands.CreateParkingOwnerCommand;
import com.spotfinder.backend.v1.profile.domain.model.valueobjects.Phone;
import com.spotfinder.backend.v1.profile.domain.model.valueobjects.Ruc;
import com.spotfinder.backend.v1.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
public class ParkingOwner extends AuditableAbstractAggregateRoot<ParkingOwner> {
    @NotBlank
    private String fullName;

    @NotBlank
    private String city;

    @NotBlank
    private String country;

    @Embedded
    private Phone phone;

    @NotBlank
    private String companyName;

    @Embedded
    private Ruc ruc;

    @NotNull(message = "UserId cannot be null")
    @Positive(message = "UserId must be positive")
    private Long userId;

    public ParkingOwner(CreateParkingOwnerCommand command, Long userId) {
        this.fullName = command.fullName();
        this.city = command.city();
        this.country = command.country();
        this.phone = new Phone(command.phone());;
        this.companyName = command.companyName();
        this.ruc = new Ruc(command.ruc());
        this.userId = userId;
    }

    public ParkingOwner() {}

    // Explicit getter methods to ensure they're available
    public String getFullName() {
        return fullName;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public String getCompanyName() {
        return companyName;
    }

    public Long getUserId() {
        return userId;
    }

    public String getPhone(){
        return phone.phone();
    }

    public String getRuc(){
        return ruc.ruc();
    }
}
