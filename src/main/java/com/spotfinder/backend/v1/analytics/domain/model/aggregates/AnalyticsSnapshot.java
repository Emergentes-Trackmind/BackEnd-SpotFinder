package com.spotfinder.backend.v1.analytics.domain.model.aggregates;

import com.spotfinder.backend.v1.analytics.domain.model.TotalsKpiDTO;
import com.spotfinder.backend.v1.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Persisted snapshot for analytics totals KPIs.
 * This lets the module own a table while still computing values from other contexts.
 */
@Entity
@Table(name = "analytics_snapshots")
@Getter
@NoArgsConstructor
public class AnalyticsSnapshot extends AuditableAbstractAggregateRoot<AnalyticsSnapshot> {

    // totalRevenue
    @Column(name = "total_revenue_value")
    private double totalRevenueValue;
    @Column(name = "total_revenue_currency")
    private String totalRevenueCurrency;
    @Column(name = "total_revenue_delta_percentage")
    private double totalRevenueDeltaPercentage;
    @Column(name = "total_revenue_delta_text")
    private String totalRevenueDeltaText;

    // occupiedSpaces
    @Column(name = "occupied_spaces_occupied")
    private int occupiedSpacesOccupied;
    @Column(name = "occupied_spaces_total")
    private int occupiedSpacesTotal;
    @Column(name = "occupied_spaces_percentage")
    private int occupiedSpacesPercentage;
    @Column(name = "occupied_spaces_text")
    private String occupiedSpacesText;

    // activeUsers
    @Column(name = "active_users_count")
    private int activeUsersCount;
    @Column(name = "active_users_delta_percentage")
    private double activeUsersDeltaPercentage;
    @Column(name = "active_users_delta_text")
    private String activeUsersDeltaText;

    // registeredParkings
    @Column(name = "registered_parkings_total")
    private int registeredParkingsTotal;
    @Column(name = "registered_parkings_new_this_month")
    private int registeredParkingsNewThisMonth;
    @Column(name = "registered_parkings_delta_text")
    private String registeredParkingsDeltaText;

    public AnalyticsSnapshot(TotalsKpiDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("TotalsKpiDTO cannot be null");
        }

        // Revenue data
        var revenue = dto.revenue();
        if (revenue != null) {
            this.totalRevenueValue = revenue.value();
            this.totalRevenueCurrency = revenue.currency();
            this.totalRevenueDeltaPercentage = revenue.deltaPercentage();
            this.totalRevenueDeltaText = revenue.deltaText();
        } else {
            this.totalRevenueValue = 0.0;
            this.totalRevenueCurrency = "USD";
            this.totalRevenueDeltaPercentage = 0.0;
            this.totalRevenueDeltaText = "Sin cambios";
        }

        // Occupied spaces data
        var spaces = dto.occupancy();
        if (spaces != null) {
            this.occupiedSpacesOccupied = spaces.occupied();
            this.occupiedSpacesTotal = spaces.total();
            this.occupiedSpacesPercentage = spaces.percentage();
            this.occupiedSpacesText = spaces.text();
        } else {
            this.occupiedSpacesOccupied = 0;
            this.occupiedSpacesTotal = 0;
            this.occupiedSpacesPercentage = 0;
            this.occupiedSpacesText = "0 lugares ocupados";
        }

        // Active users data
        var users = dto.users();
        if (users != null) {
            this.activeUsersCount = users.total();
            this.activeUsersDeltaPercentage = users.deltaPercentage();
            this.activeUsersDeltaText = users.text();
        } else {
            this.activeUsersCount = 0;
            this.activeUsersDeltaPercentage = 0.0;
            this.activeUsersDeltaText = "Sin usuarios activos";
        }

        // Registered parkings data
        var parkings = dto.parkings();
        if (parkings != null) {
            this.registeredParkingsTotal = parkings.total();
            this.registeredParkingsNewThisMonth = parkings.newCount();
            this.registeredParkingsDeltaText = parkings.text();
        } else {
            this.registeredParkingsTotal = 0;
            this.registeredParkingsNewThisMonth = 0;
            this.registeredParkingsDeltaText = "Sin parkings registrados";
        }
    }
}
