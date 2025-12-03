package com.spotfinder.backend.v1.parkingManagement.domain.model.aggregates;

import com.spotfinder.backend.v1.parkingManagement.domain.model.commands.AddParkingSpotCommand;
import com.spotfinder.backend.v1.parkingManagement.domain.model.commands.CreateParkingCommand;
import com.spotfinder.backend.v1.parkingManagement.domain.model.entities.ParkingSpot;
import com.spotfinder.backend.v1.parkingManagement.domain.model.valueobjects.OwnerId;
import com.spotfinder.backend.v1.parkingManagement.domain.model.valueobjects.SpotManager;
import com.spotfinder.backend.v1.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Entity
public class Parking extends AuditableAbstractAggregateRoot<Parking> {

    @Embedded
    private OwnerId ownerId;

    @Getter @Setter
    @NotNull
    private String name;

    @Getter @Setter
    @NotNull
    private String description;

    // Optional compatibility fields for FE json format
    @Getter @Setter
    private String type;

    @Getter @Setter
    private String phone;

    @Getter @Setter
    private String email;

    @Getter @Setter
    private String website;

    @Getter @Setter
    private String status = "Activo";

    @Getter @Setter
    @NotNull
    private String address;

    @Getter @Setter
    @NotNull
    private Double lat;

    @Getter @Setter
    @NotNull
    private Double lng;

    @Getter
    @Setter
    @NotNull
    private Float ratePerHour;

    @Getter
    @Setter
    private Float rating;

    @Getter
    @Setter
    private Float ratingCount;

    @Getter
    private Float averageRating;

    @Getter
    @Setter
    @NotNull
    private Integer totalSpots;

    @Getter
    @Setter
    @NotNull
    private Integer availableSpots;

    @Getter
    @Setter
    @NotNull
    private Integer totalRows;

    @Getter
    @Setter
    @NotNull
    private Integer totalColumns;

    @Getter
    @Setter
    @NotNull
    private String imageUrl;

    @Getter
    @Setter
    @jakarta.persistence.Lob
    private String locationJson;

    @Getter
    @Setter
    @jakarta.persistence.Lob
    private String pricingJson;

    @Getter
    @Setter
    @jakarta.persistence.Lob
    private String featuresJson;

    @Embedded
    private SpotManager parkingSpotManager;

    protected Parking() {}

    public Parking(CreateParkingCommand command) {
        this.ownerId = new OwnerId(command.ownerId());
        this.name = command.name();
        this.description = command.description();
        this.type = command.type() != null ? command.type() : "Comercial";
        this.phone = command.phone() != null ? command.phone() : "";
        this.email = command.email() != null ? command.email() : "";
        this.website = command.website() != null ? command.website() : "";
        this.status = command.status() != null ? command.status() : "Activo";
        this.address = command.address();
        this.lat = command.lat();
        this.lng = command.lng();
        this.ratePerHour = command.ratePerHour();
        this.rating = 0f;
        this.ratingCount = 0f;
        this.averageRating = 0f;
        this.totalSpots = command.totalSpots();
        this.availableSpots = command.availableSpots();
        this.totalRows = command.totalRows();
        this.totalColumns = command.totalColumns();
        this.imageUrl = command.imageUrl();
        this.parkingSpotManager = new SpotManager();
    }

    public void setRating(Float rating) {
        this.rating += rating;
        this.ratingCount += 1;
        this.averageRating = this.rating / this.ratingCount;
    }

    public ParkingSpot addParkingSpot(AddParkingSpotCommand command) {
       return parkingSpotManager.addParkingSpot(this, command.row(), command.column(), command.label());
    }

    public List<ParkingSpot> getParkingSpots() {
        return parkingSpotManager.getParkingSpots();
    }

    public ParkingSpot getParkingSpot(UUID parkingSpotId) {
        return parkingSpotManager.getParkingSpotById(parkingSpotId);
    }

    public void updateAvailableSpotsCount(Integer numberAvailable, String operation) {
        if (operation.equals("add")) {
            this.availableSpots += numberAvailable;
        } else if (operation.equals("subtract")) {
            this.availableSpots -= numberAvailable;
        } else {
            throw new IllegalArgumentException("Invalid operation");
        }
    }

    public Long getOwnerId() {
        return this.ownerId.getValue();
    }

    // Explicit getter methods to ensure they're available
    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getWebsite() {
        return website;
    }

    public String getLocationJson() {
        return locationJson;
    }

    public void setLocationJson(String locationJson) {
        this.locationJson = locationJson;
    }

    public String getPricingJson() {
        return pricingJson;
    }

    public void setPricingJson(String pricingJson) {
        this.pricingJson = pricingJson;
    }

    public String getFeaturesJson() {
        return featuresJson;
    }

    public void setFeaturesJson(String featuresJson) {
        this.featuresJson = featuresJson;
    }

    public Float getRatePerHour() {
        return ratePerHour;
    }

    public Float getRating() {
        return rating;
    }

    public Integer getTotalSpots() {
        return totalSpots;
    }

    public Integer getAvailableSpots() {
        return availableSpots;
    }

    public Float getAverageRating() {
        return averageRating;
    }

    // Additional missing getter methods
    public String getDescription() {
        return description;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }

    public Float getRatingCount() {
        return ratingCount;
    }

    public Integer getTotalRows() {
        return totalRows;
    }

    public Integer getTotalColumns() {
        return totalColumns;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    // Missing setter methods
    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public void setTotalSpots(Integer totalSpots) {
        this.totalSpots = totalSpots;
    }

    public void setAvailableSpots(Integer availableSpots) {
        this.availableSpots = availableSpots;
    }

    public void setRatePerHour(Float ratePerHour) {
        this.ratePerHour = ratePerHour;
    }

}
