package com.spotfinder.backend.v1.analytics.application.internal.queryservices;

import com.spotfinder.backend.v1.analytics.application.internal.outboundservices.acl.AnalyticsExternalParkingService;
import com.spotfinder.backend.v1.analytics.application.internal.outboundservices.acl.ExternalReservationsService;
import com.spotfinder.backend.v1.analytics.application.internal.outboundservices.acl.AnalyticsExternalUserService;
import com.spotfinder.backend.v1.analytics.domain.model.*;
import com.spotfinder.backend.v1.analytics.domain.services.AnalyticsQueryService;
import com.spotfinder.backend.v1.analytics.domain.model.queries.GetTotalsKpiQuery;
import com.spotfinder.backend.v1.analytics.domain.model.queries.GetRevenueByMonthQuery;
import com.spotfinder.backend.v1.analytics.domain.model.queries.GetOccupancyByHourQuery;
import com.spotfinder.backend.v1.analytics.domain.model.queries.GetActivityQuery;
import com.spotfinder.backend.v1.analytics.domain.model.queries.GetTopParkingsQuery;
import com.spotfinder.backend.v1.parkingManagement.domain.model.aggregates.Parking;
import com.spotfinder.backend.v1.reservations.domain.model.aggregates.Reservation;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsQueryServiceImpl implements AnalyticsQueryService {

    private final ExternalReservationsService reservationsService;
    private final AnalyticsExternalParkingService parkingService;
    private final AnalyticsExternalUserService userService;

    public AnalyticsQueryServiceImpl(ExternalReservationsService reservationsService,
                                     AnalyticsExternalParkingService parkingService,
                                     AnalyticsExternalUserService userService) {
        this.reservationsService = reservationsService;
        this.parkingService = parkingService;
        this.userService = userService;
    }

    private List<Parking> getParkings(Long profileId) {
        try {
            if (profileId != null) {
                return parkingService.findById(profileId);
            }
            return parkingService.findAll();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    // The parking ACL already filters by the authenticated owner.
    // No need to fetch the userId explicitly here.

    @Override
    public TotalsKpiDTO getTotals() {
        return getTotals(null);
    }

    public TotalsKpiDTO getTotals(Long profileId) {
        System.out.println("📊 [Analytics] getTotals() called with profileId: " + profileId);
        
        try {
            LocalDateTime from = LocalDateTime.now().minusMonths(1);
            
            // Obtener los parkings según el filtro
            System.out.println("🔍 [Analytics] Fetching parkings...");
            List<Parking> parkings = getParkings(profileId);
            if (parkings == null) parkings = new ArrayList<>();
            
            System.out.println("✅ [Analytics] Parkings obtained: " + parkings.size());
            
            List<Long> parkingIds = parkings.stream()
                    .filter(p -> p.getId() != null)
                    .map(Parking::getId)
                    .collect(Collectors.toList());

            System.out.println("📝 [Analytics] Parking IDs: " + parkingIds);

            // Si no hay parkings, devolver valores por defecto
            if (parkingIds.isEmpty()) {
                System.out.println("⚠️ [Analytics] No parkings found, returning empty DTO");
                return createEmptyTotalsKpiDTO();
            }

        // Filtrar reservas por los parkings aplicables
        List<Reservation> allReservations = reservationsService.findAll().stream()
                .filter(r -> parkingIds.contains(r.getParkingId()))
                .collect(Collectors.toList());

        // Calcular ingresos del mes actual y anterior
        double currentRevenue = allReservations.stream()
                .filter(r -> r.getCreatedAt() != null && toLdt(r.getCreatedAt()).isAfter(from))
                .mapToDouble(r -> r.getTotalPrice() != null ? r.getTotalPrice() : 0.0)
                .sum();

        double previousRevenue = allReservations.stream()
                .filter(r -> r.getCreatedAt() != null && 
                        toLdt(r.getCreatedAt()).isBefore(from) && 
                        toLdt(r.getCreatedAt()).isAfter(from.minusMonths(1)))
                .mapToDouble(r -> r.getTotalPrice() != null ? r.getTotalPrice() : 0.0)
                .sum();

        double deltaPerc = previousRevenue == 0 ? 100.0 : ((currentRevenue - previousRevenue) / previousRevenue) * 100;
        String deltaText = String.format("%+.1f%% respecto al periodo anterior", deltaPerc);
        String revenueText = String.format("$%.2f este mes", currentRevenue);

        var revenue = new TotalsKpiDTO.RevenueKpi(
            round2(currentRevenue), 
            "USD", 
            round1(deltaPerc), 
            deltaText,
            revenueText);

        // Calcular estadísticas de ocupación
        int totalSpots = parkings.stream()
                .mapToInt(p -> p.getTotalSpots() != null ? p.getTotalSpots() : 0)
                .sum();
        int availableSpots = parkings.stream()
                .mapToInt(p -> p.getAvailableSpots() != null ? p.getAvailableSpots() : 0)
                .sum();
        int occupied = Math.max(0, totalSpots - availableSpots);
        double occupancyRate = totalSpots > 0 ? (occupied * 100.0) / totalSpots : 0;
        int percentage = (int) Math.round(occupancyRate);
        String occupancyText = String.format("%d lugares ocupados", occupied);
        String occupancyDeltaText = String.format("%d%% de ocupación", percentage);
        
        var occupancy = new TotalsKpiDTO.OccupiedSpacesKpi(
            occupied, 
            totalSpots, 
            percentage, 
            occupancyText,
            occupancyDeltaText);
        
        // Calcular estadísticas de usuariosC:\Prueba\TrackMind> analiza las carpetas de esa ruta, verifica el frontend Angular y analiza el backend, como funcionan, y corrige el error del backend
        // Obtener usuarios únicos y sus primeras reservas
        Map<Long, LocalDateTime> userFirstReservations = new HashMap<>();
        
        // Procesar cada reserva para encontrar la primera por usuario
        for (Reservation r : allReservations) {
            if (r.getDriverId() != null && r.getCreatedAt() != null) {
                LocalDateTime reservationDate = toLdt(r.getCreatedAt());
                userFirstReservations.merge(
                    r.getDriverId(),
                    reservationDate,
                    (existing, newDate) -> existing.isBefore(newDate) ? existing : newDate
                );
            }
        }

        // Usuarios totales (todos los que tienen al menos una reserva)
        Set<Long> allUniqueUserIds = userFirstReservations.keySet();

        // Usuarios nuevos (solo aquellos cuya primera reserva fue en el último mes)
        Set<Long> newUniqueUserIds = userFirstReservations.entrySet().stream()
                .filter(e -> e.getValue().isAfter(from))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        int totalUsers = allUniqueUserIds.size();
        int newUsers = newUniqueUserIds.size();
        
        // Calcular el porcentaje de usuarios nuevos
        double usersDelta = totalUsers == 0 ? 0.0 : (newUsers * 100.0) / totalUsers;
        
        // Crear el KPI de usuarios activos
        String usersText = String.format("%d usuarios activos", totalUsers);
        
        var users = new TotalsKpiDTO.ActiveUsersKpi(
            totalUsers,
            round1(usersDelta),
            usersText,
            newUsers
        );

        // Estadísticas de parkings
        int totalP = parkings.size();
        
        // Calcular parkings nuevos este mes
        LocalDateTime startOfMonth = from.withDayOfMonth(1);
        long newThisMonth = parkings.stream()
                .filter(p -> p.getCreatedAt() != null)
                .filter(p -> toLdt(p.getCreatedAt()).isAfter(startOfMonth))
                .count();

        // Preparar textos descriptivos
        String parkingText = totalP == 1 
            ? "1 parking registrado" 
            : String.format("%d parkings registrados", totalP);

        // Calcular porcentaje de parkings nuevos
        double parkingDeltaPerc = totalP == 0 
            ? 0.0 
            : Math.min(100.0, (newThisMonth * 100.0) / totalP);

        // Crear el DTO de parkings
        var parkingsKpi = new TotalsKpiDTO.RegisteredParkingsKpi(
            totalP,
            (int) newThisMonth,
            parkingText,
            round1(parkingDeltaPerc)
        );

        // Retornar el DTO completo
        return new TotalsKpiDTO(revenue, occupancy, users, parkingsKpi);
        } catch (Exception e) {
            // Logging del error para diagnóstico
            System.err.println("❌ [Analytics] Error en getTotals(): " + e.getMessage());
            e.printStackTrace();
            // Si ocurre cualquier error, devolver un DTO vacío para evitar errores en el frontend
            return createEmptyTotalsKpiDTO();
        }
    }

    // Query handlers
    @Override
    public TotalsKpiDTO handle(GetTotalsKpiQuery query) { return getTotals(); }

    @Override
    public List<RevenueByMonthDTO> getRevenue() {
        return getRevenue(null);
    }

    public List<RevenueByMonthDTO> getRevenue(Long parkingId) {
        // Obtener los parkings según el filtro
        List<Long> parkingIds = getParkings(parkingId).stream()
                .map(Parking::getId)
                .collect(Collectors.toList());
                
        // Obtener y filtrar las reservas
        List<Reservation> all = reservationsService.findAll().stream()
                .filter(r -> parkingIds.contains(r.getParkingId()))
                .collect(Collectors.toList());
                
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");
        Map<String, Double> byMonth = all.stream()
                .filter(r -> r.getCreatedAt() != null)
                .collect(Collectors.groupingBy(r -> toLdt(r.getCreatedAt()).format(fmt),
                        Collectors.summingDouble(r -> r.getTotalPrice() != null ? r.getTotalPrice() : 0d)));
        
        return byMonth.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new RevenueByMonthDTO(e.getKey(), round2(e.getValue()), "USD"))
                .collect(Collectors.toList());
    }

    @Override
    public List<RevenueByMonthDTO> handle(GetRevenueByMonthQuery query) { return getRevenue(); }

    @Override
    public List<OccupancyByHourDTO> getOccupancy() {
        return getOccupancy(null);
    }

    public List<OccupancyByHourDTO> getOccupancy(Long parkingId) {
        LocalDate today = LocalDate.now();
        
        // Obtener los parkings según el filtro
        List<Parking> parkings = getParkings(parkingId);
        List<Long> parkingIds = parkings.stream().map(Parking::getId).collect(Collectors.toList());

        List<Reservation> todayRes = reservationsService.findAll().stream()
                .filter(r -> parkingIds.contains(r.getParkingId()))
                .filter(r -> r.getDate() != null && r.getDate().isEqual(today))
                .collect(Collectors.toList());
        int totalSpots = parkings.stream().mapToInt(p -> p.getTotalSpots() != null ? p.getTotalSpots() : 0).sum();
        if (totalSpots <= 0) totalSpots = 1;

        List<OccupancyByHourDTO> list = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            final int h = hour;
            long occupiedSpots = todayRes.stream()
                    .filter(r -> r.getStartTime() != null && r.getEndTime() != null)
                    .filter(r -> r.getStartTime().getHour() <= h && r.getEndTime().getHour() > h)
                    .map(Reservation::getParkingSpotId)
                    .distinct()
                    .count();
            int occupied = (int) occupiedSpots;
            int perc = (int) Math.round((occupied * 100.0) / totalSpots);
            list.add(new OccupancyByHourDTO(hour, perc, occupied, totalSpots));
        }
        return list;
    }

    @Override
    public List<OccupancyByHourDTO> handle(GetOccupancyByHourQuery query) { return getOccupancy(); }

    @Override
    public List<ActivityItemDTO> getActivity() {
        return getActivity(null);
    }

    public List<ActivityItemDTO> getActivity(Long parkingId) {
        List<ActivityItemDTO> items = new ArrayList<>();
        
        // Obtener los parkings según el filtro
        List<Parking> parkings = getParkings(parkingId);
        List<Long> parkingIds = parkings.stream().map(Parking::getId).collect(Collectors.toList());

        parkings.stream()
                .sorted(Comparator.comparing(Parking::getCreatedAt, Comparator.nullsLast(Date::compareTo)).reversed())
                .limit(5)
                .forEach(p -> items.add(new ActivityItemDTO(
                        String.valueOf(p.getId()),
                        "parking_created",
                        "Parking registrado",
                        p.getName() + " agregado",
                        "Sistema",
                        null,
                        "created",
                        toIso(p.getCreatedAt()),
                        new ActivityItemDTO.RelatedEntity(String.valueOf(p.getId()), p.getName(), "parking")
                )));

        reservationsService.findAll().stream()
                .filter(r -> parkingIds.contains(r.getParkingId()))
                .sorted(Comparator.comparing(Reservation::getCreatedAt, Comparator.nullsLast(Date::compareTo)).reversed())
                .limit(5)
                .forEach(r -> items.add(new ActivityItemDTO(
                        String.valueOf(r.getId()),
                        "reservation_confirmed",
                        "Reserva registrada",
                        r.getSpotLabel() != null ? r.getSpotLabel() : "Reserva",
                        r.getDriverName() != null ? r.getDriverName() : "Usuario",
                        null,
                        "confirmed",
                        toIso(r.getCreatedAt()),
                        new ActivityItemDTO.RelatedEntity(String.valueOf(r.getParkingId()), r.getSpotLabel() != null ? r.getSpotLabel() : "Reserva", "reservation")
                )));

        return items.stream()
                .sorted(Comparator.comparing(ActivityItemDTO::createdAt).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    @Override
    public List<ActivityItemDTO> handle(GetActivityQuery query) { return getActivity(); }

    @Override
    public List<TopParkingDTO> getTopParkings() {
        try {
            LocalDateTime from = LocalDateTime.now().minusDays(30);
            
            // Obtener parkings del usuario autenticado (ACL aplica el filtro)
            List<Parking> userParkings = getParkings(null);
            if (userParkings.isEmpty()) {
                return new ArrayList<>();
            }

            List<Long> parkingIds = userParkings.stream()
                .map(Parking::getId)
                .collect(Collectors.toList());

            // Obtener reservas solo de los parkings del usuario
            List<Reservation> lastRes = reservationsService.findAll().stream()
                .filter(r -> r.getCreatedAt() != null && 
                            toLdt(r.getCreatedAt()).isAfter(from) &&
                            parkingIds.contains(r.getParkingId()))
                .collect(Collectors.toList());

            // Calcular ingresos por parking
            Map<Long, Double> revenueByParking = lastRes.stream()
                .collect(Collectors.groupingBy(
                    Reservation::getParkingId, 
                    Collectors.summingDouble(r -> r.getTotalPrice() != null ? r.getTotalPrice() : 0d)
                ));

            // Mapear a DTOs solo los parkings del usuario
            return userParkings.stream()
                .map(p -> {
                    int total = p.getTotalSpots() != null ? p.getTotalSpots() : 0;
                    int available = p.getAvailableSpots() != null ? p.getAvailableSpots() : 0;
                    int occupancy = total > 0 ? (int) Math.round(((total - available) * 100.0) / total) : 0;
                    double revenue = revenueByParking.getOrDefault(p.getId(), 0d);
                    double rating = p.getAverageRating() != null ? p.getAverageRating() : 0d;
                    String status = mapStatus(p.getStatus());
                    return new TopParkingDTO(
                        String.valueOf(p.getId()), 
                        p.getName(), 
                        occupancy, 
                        round1(rating), 
                        round2(revenue), 
                        "USD", 
                        p.getAddress(), 
                        status
                    );
                })
                .sorted(Comparator.comparing(TopParkingDTO::monthlyRevenue).reversed())
                .limit(5)
                .collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public List<TopParkingDTO> handle(GetTopParkingsQuery query) { return getTopParkings(); }

    private static LocalDateTime toLdt(Date date) {
        return date != null ? LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()) : LocalDateTime.now();
    }
    
    private static String mapStatus(String status) {
        if (status == null) return "active";
        String s = status.trim().toLowerCase();
        return switch (s) {
            case "activo", "active" -> "active";
            case "mantenimiento", "maintenance" -> "maintenance";
            case "inactivo", "inactive" -> "inactive";
            default -> "active";
        };
    }
    
    private static String toIso(Date date) { 
        return date != null ? date.toInstant().toString() : Instant.now().toString(); 
    }
    
    private static double round2(double v) { 
        return Math.round(v * 100.0) / 100.0; 
    }
    
    private static double round1(double v) { 
        return Math.round(v * 10.0) / 10.0; 
    }
    
    private TotalsKpiDTO createEmptyTotalsKpiDTO() {
        var revenue = new TotalsKpiDTO.RevenueKpi(0.0, "USD", 0.0, "Sin cambios", "$0 este mes");
        var occupancy = new TotalsKpiDTO.OccupiedSpacesKpi(0, 0, 0, "0 lugares ocupados", "Sin cambios");
        var users = new TotalsKpiDTO.ActiveUsersKpi(0, 0.0, "Sin usuarios activos", 0);
        var parkings = new TotalsKpiDTO.RegisteredParkingsKpi(0, 0, "Sin parkings registrados", 0.0);
        return new TotalsKpiDTO(revenue, occupancy, users, parkings);
    }
}
