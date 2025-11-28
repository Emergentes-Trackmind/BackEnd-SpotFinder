package com.spotfinder.backend.v1.analytics.interfaces.rest;

import com.spotfinder.backend.v1.analytics.domain.services.AnalyticsQueryService;
import com.spotfinder.backend.v1.analytics.domain.model.*;
import com.spotfinder.backend.v1.analytics.domain.model.queries.*;
import com.spotfinder.backend.v1.analytics.interfaces.rest.resources.*;
import com.spotfinder.backend.v1.analytics.interfaces.rest.transform.AnalyticsResourceAssemblers;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@RestController
@RequestMapping(value = {"/api/analytics", "/api/v1/analytics"})
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Analytics", description = "Analytics endpoints for dashboard KPIs and charts")
public class AnalyticsController {

    private final AnalyticsQueryService analyticsService;
    private final java.util.concurrent.ConcurrentHashMap<String, Object> cache = new java.util.concurrent.ConcurrentHashMap<>();

    // Explicit constructor to fix compilation issue
    public AnalyticsController(AnalyticsQueryService analyticsService) {
        this.analyticsService = analyticsService;
    }

    private String userCacheKey(String base) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        String user = auth.getName() != null ? auth.getName() : "anonymous";
        return "user:" + user + ":" + base;
    }

    // Compatibility endpoint for FE requests like: GET /api/analytics?profileId=123
    // Returns a single summary object matching the FE expected structure used by Angular.
    public record KpiValue(double value, double trend) {}
    public record FeKpis(KpiValue avgOccupation, KpiValue monthlyRevenue, KpiValue uniqueUsers, KpiValue avgTime) {}
    public record HourlyOccupationItem(String hour, int percentage) {}
    public record ActivityItem(String action, String details, String timeAgo) {}
    public record AnalyticsSummary(String id, String profileId,
                                   FeKpis kpis,
                                   java.util.List<HourlyOccupationItem> hourlyOccupation,
                                   java.util.List<ActivityItem> recentActivity) {}

    @GetMapping
    public ResponseEntity<java.util.List<AnalyticsSummary>> getAnalytics(@RequestParam(required = false) String profileId) {
        Long parkingId = null;
        try {
            parkingId = profileId != null ? Long.parseLong(profileId) : null;
        } catch (NumberFormatException e) {
            // Si el ID no es válido, retornamos datos vacíos
            return ResponseEntity.ok(java.util.List.of());
        }

        var totalsDto = analyticsService.getTotals(parkingId);
        var occupancyDto = analyticsService.getOccupancy(parkingId);
        var activityDto = analyticsService.getActivity(parkingId);

        // Map TotalsKpiDTO -> FE KPIs shape expected by Angular page
        var kpis = new FeKpis(
                // avgOccupation: percentage with neutral trend (no historical baseline available)
                new KpiValue(totalsDto.occupancy().percentage(), 0),
                // monthlyRevenue: value and trend from revenue delta
                new KpiValue(totalsDto.revenue().value(), totalsDto.revenue().deltaPercentage()),
                // uniqueUsers: total and delta
                new KpiValue(totalsDto.users().total(), totalsDto.users().deltaPercentage()),
                // avgTime: no direct data on BE; default to 0
                new KpiValue(0, 0)
        );

        // Map occupancy to [ { hour: "HH:00", percentage } ]
        var hourly = occupancyDto.stream()
                .map(o -> new HourlyOccupationItem(String.format("%02d:00", o.hour()), o.percentage()))
                .toList();

        // Map activity to [ { action, details, timeAgo } ]
        var recent = activityDto.stream()
                .map(a -> new ActivityItem(
                        a.title(),
                        a.description(),
                        a.createdAt()
                ))
                .toList();

        var id = profileId != null ? profileId : "global";
        var summary = new AnalyticsSummary(id, id, kpis, hourly, recent);
        return ResponseEntity.ok(java.util.List.of(summary));
    }

    @GetMapping("/totals")
    public ResponseEntity<TotalsKpiResource> getTotals() {
        var key = userCacheKey("totals");
        if (key != null) {
            var cached = (TotalsKpiResource) cache.get(key);
            if (cached != null) return ResponseEntity.ok(cached);
        }
        var dto = analyticsService.handle(new GetTotalsKpiQuery());
        var res = AnalyticsResourceAssemblers.toResource(dto);
        if (key != null) cache.put(key, res);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/revenue")
    public ResponseEntity<List<RevenueByMonthResource>> getRevenue() {
        var key = userCacheKey("revenue");
        if (key != null) {
            var cached = (List<RevenueByMonthResource>) cache.get(key);
            if (cached != null) return ResponseEntity.ok(cached);
        }
        var list = analyticsService.handle(new GetRevenueByMonthQuery());
        var res = AnalyticsResourceAssemblers.toRevenueResources(list);
        if (key != null) cache.put(key, res);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/occupancy")
    public ResponseEntity<List<OccupancyByHourResource>> getOccupancy() {
        var key = userCacheKey("occupancy");
        if (key != null) {
            var cached = (List<OccupancyByHourResource>) cache.get(key);
            if (cached != null) return ResponseEntity.ok(cached);
        }
        var list = analyticsService.handle(new GetOccupancyByHourQuery());
        var res = AnalyticsResourceAssemblers.toOccupancyResources(list);
        if (key != null) cache.put(key, res);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/activity")
    public ResponseEntity<List<ActivityItemResource>> getActivity() {
        var key = userCacheKey("activity");
        if (key != null) {
            var cached = (List<ActivityItemResource>) cache.get(key);
            if (cached != null) return ResponseEntity.ok(cached);
        }
        var list = analyticsService.handle(new GetActivityQuery());
        var res = AnalyticsResourceAssemblers.toActivityResources(list);
        if (key != null) cache.put(key, res);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/top-parkings")
    public ResponseEntity<List<TopParkingResource>> getTopParkings() {
        var key = userCacheKey("top-parkings");
        if (key != null) {
            var cached = (List<TopParkingResource>) cache.get(key);
            if (cached != null) return ResponseEntity.ok(cached);
        }
        var list = analyticsService.handle(new GetTopParkingsQuery());
        var res = AnalyticsResourceAssemblers.toTopParkingResources(list);
        if (key != null) cache.put(key, res);
        return ResponseEntity.ok(res);
    }
}
