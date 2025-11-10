package com.spotfinder.backend.v1.analytics.interfaces.rest.transform;

import com.spotfinder.backend.v1.analytics.domain.model.*;
import com.spotfinder.backend.v1.analytics.interfaces.rest.resources.*;

import java.util.List;
import java.util.stream.Collectors;

public class AnalyticsResourceAssemblers {

    public static TotalsKpiResource toResource(TotalsKpiDTO dto) {
        try {
            return new TotalsKpiResource(
                // Revenue
                new TotalsKpiResource.RevenueKpi(
                    dto.revenue().value(),
                    dto.revenue().currency(),
                    dto.revenue().deltaPercentage(),
                    dto.revenue().deltaText(),
                    dto.revenue().text()
                ),
                // Occupancy
                new TotalsKpiResource.OccupancyKpi(
                    dto.occupancy().occupied(),
                    dto.occupancy().total(),
                    dto.occupancy().percentage(),
                    dto.occupancy().text(),
                    dto.occupancy().deltaText()
                ),
                // Users
                new TotalsKpiResource.UsersKpi(
                    dto.users().total(),
                    dto.users().deltaPercentage(),
                    dto.users().text(),
                    dto.users().newUsers()
                ),
                // Parkings
                new TotalsKpiResource.ParkingsKpi(
                    dto.parkings().total(),
                    dto.parkings().newCount(),
                    dto.parkings().text(),
                    dto.parkings().deltaPercentage()
                )
            );
        } catch (Exception e) {
            // Si hay algún error, devolver un recurso con valores por defecto
            return createEmptyResource();
        }
    }

    private static TotalsKpiResource createEmptyResource() {
        return new TotalsKpiResource(
            new TotalsKpiResource.RevenueKpi(0.0, "USD", 0.0, "Sin cambios", "$0 este mes"),
            new TotalsKpiResource.OccupancyKpi(0, 0, 0, "0 lugares ocupados", "0% de ocupación"),
            new TotalsKpiResource.UsersKpi(0, 0.0, "Sin usuarios activos", 0),
            new TotalsKpiResource.ParkingsKpi(0, 0, "Sin parkings registrados", 0.0)
        );
    }

    public static List<RevenueByMonthResource> toRevenueResources(List<RevenueByMonthDTO> list) {
        return list.stream().map(d -> new RevenueByMonthResource(d.month(), d.revenue(), d.currency())).collect(Collectors.toList());
    }

    public static List<OccupancyByHourResource> toOccupancyResources(List<OccupancyByHourDTO> list) {
        return list.stream().map(d -> new OccupancyByHourResource(d.hour(), d.percentage(), d.occupied(), d.total())).collect(Collectors.toList());
    }

    public static List<ActivityItemResource> toActivityResources(List<ActivityItemDTO> list) {
        return list.stream().map(d -> new ActivityItemResource(
                d.id(), d.type(), d.title(), d.description(), d.userName(), d.userAvatar(), d.status(), d.createdAt(),
                d.relatedEntity() != null ? new ActivityItemResource.RelatedEntity(d.relatedEntity().id(), d.relatedEntity().name(), d.relatedEntity().type()) : null
        )).collect(Collectors.toList());
    }

    public static List<TopParkingResource> toTopParkingResources(List<TopParkingDTO> list) {
        return list.stream().map(d -> new TopParkingResource(d.id(), d.name(), d.occupancyPercentage(), d.rating(), d.monthlyRevenue(), d.currency(), d.address(), d.status())).collect(Collectors.toList());
    }
}

