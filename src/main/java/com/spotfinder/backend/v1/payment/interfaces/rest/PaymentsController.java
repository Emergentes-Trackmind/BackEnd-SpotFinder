package com.spotfinder.backend.v1.payment.interfaces.rest;

import com.spotfinder.backend.v1.iam.infrastructure.tokens.jwt.BearerTokenService;
import com.spotfinder.backend.v1.payment.domain.model.valueobjects.Payment;
import com.spotfinder.backend.v1.payment.infrastructure.persistence.jpa.repositories.PaymentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = {"/api/v1/payments", "/api/billing"}, produces = APPLICATION_JSON_VALUE)
@Tag(name = "Billing", description = "Billing endpoints compatible with SpotFinder frontend")
public class PaymentsController {

    private final BearerTokenService tokenService;
    private final PaymentRepository paymentRepository;

    public PaymentsController(BearerTokenService tokenService, PaymentRepository paymentRepository) {
        this.tokenService = tokenService;
        this.paymentRepository = paymentRepository;
    }

    // ====== DTOs para FE ======
    public record Plan(String id, String code, String name, double price, String currency,
                       int parkingLimit, int iotLimit, String priceId, List<String> features) {}

    public record SubscriptionInfo(Plan plan, String status, String startDate, String renewalDate,
                                   String stripeCustomerId, Boolean cancelAtPeriodEnd) {}

    public record PaymentRow(String id, String paidAt, double amount, String currency,
                             String status, String transactionId) {}

    public record CheckoutSessionResponse(String url, String sessionId) {}
    public record PortalSessionResponse(String url) {}

    // In-memory estado por usuario
    private static final Map<String, SubscriptionInfo> SUBSCRIPTIONS = new HashMap<>();

    private static final List<Plan> PLANS = List.of(
            new Plan("plan_basic", "BASIC", "Basic", 0.0, "USD", 1, 5, null, List.of("Free tier")),
            new Plan("plan_adv", "ADVANCED", "Advanced", 29.99, "USD", 10, 100, "price_123", List.of("Support", "Analytics"))
    );

    private String getUserId(HttpServletRequest request) {
        try {
            String token = tokenService.getBearerTokenFrom(request);
            return token != null ? tokenService.getUserIdFromToken(token) : null;
        } catch (Exception e) { return null; }
    }

    private Plan findPlan(String code) {
        return PLANS.stream().filter(p -> p.code().equalsIgnoreCase(code)).findFirst().orElse(null);
    }

    // ====== Endpoints ======

    @GetMapping(value = "/plans", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get billing plans", description = "Returns the list of available plans for subscription")
    public ResponseEntity<List<Plan>> getPlans() {
        return ResponseEntity.ok(PLANS);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current subscription", description = "Returns the subscription info for the authenticated user")
    public ResponseEntity<SubscriptionInfo> getMe(HttpServletRequest request) {
        String userId = getUserId(request);
        if (userId == null) return ResponseEntity.status(401).build();
        var info = SUBSCRIPTIONS.computeIfAbsent(userId, k ->
                new SubscriptionInfo(PLANS.get(0), "ACTIVE", Instant.now().toString(), null, "cus_" + userId, false)
        );
        return ResponseEntity.ok(info);
    }

    public record SubscribeRequest(String planCode) {}
    @PostMapping("/subscribe")
    @Operation(summary = "Subscribe to a plan")
    public ResponseEntity<SubscriptionInfo> subscribe(HttpServletRequest request, @RequestBody SubscribeRequest body) {
        String userId = getUserId(request);
        if (userId == null) return ResponseEntity.status(401).build();
        Plan plan = findPlan(body.planCode());
        if (plan == null) return ResponseEntity.badRequest().build();
        var info = new SubscriptionInfo(plan, "ACTIVE", Instant.now().toString(), null, "cus_" + userId, false);
        SUBSCRIPTIONS.put(userId, info);
        return ResponseEntity.ok(info);
    }

    @PostMapping("/cancel")
    @Operation(summary = "Cancel current subscription (fallback to Basic)")
    public ResponseEntity<SubscriptionInfo> cancel(HttpServletRequest request) {
        String userId = getUserId(request);
        if (userId == null) return ResponseEntity.status(401).build();
        var info = new SubscriptionInfo(PLANS.get(0), "ACTIVE", Instant.now().toString(), null, "cus_" + userId, false);
        SUBSCRIPTIONS.put(userId, info);
        return ResponseEntity.ok(info);
    }

    public record CheckoutRequest(String priceId) {}
    @PostMapping("/create-checkout-session")
    @Operation(summary = "Create Stripe checkout session")
    public ResponseEntity<CheckoutSessionResponse> checkout(@RequestBody CheckoutRequest req) {
        var res = new CheckoutSessionResponse("https://checkout.stripe.com/pay/test?priceId=" + (req.priceId()!=null?req.priceId():"price_test"),
                "cs_test_" + UUID.randomUUID());
        return ResponseEntity.ok(res);
    }

    @PostMapping("/create-portal-session")
    @Operation(summary = "Create Stripe customer portal session")
    public ResponseEntity<PortalSessionResponse> portal() {
        return ResponseEntity.ok(new PortalSessionResponse("https://billing.stripe.com/p/session/test"));
    }

    @GetMapping("/payments")
    @Operation(summary = "Get payments history")
    public ResponseEntity<List<PaymentRow>> getPayments(HttpServletRequest request) {
        String userId = getUserId(request);
        if (userId == null) return ResponseEntity.status(401).build();
        List<Payment> payments = paymentRepository.findByUserId(Long.valueOf(userId));
        List<PaymentRow> list = payments.stream().map(p -> new PaymentRow(
                String.valueOf(p.getId()),
                p.getPaidAt() != null ? p.getPaidAt().toString() : Instant.now().toString(),
                p.getAmount(),
                "USD",
                p.isForSubscription() ? "SUCCEEDED" : "SUCCEEDED",
                "txn_" + p.getId()
        )).toList();
        return ResponseEntity.ok(list);
    }
}
