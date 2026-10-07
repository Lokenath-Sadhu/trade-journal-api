package dev.tradejournal.api.sizing;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;


@Component
public final class PositionSizer {

    private final BigDecimal maxRiskPercent;

    public PositionSizer(SizingProperties properties) {
        System.out.println("PositionSizer created");
        this.maxRiskPercent = properties.maxRiskPercent();
    }
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    public SizingResult size(SizingRequest r) {
        var problem = validate(r);
        if (problem.isPresent()) {
            return problem.get();
        }

        // 1. How much money am I willing to lose?
        BigDecimal riskAmount =r.capital().multiply(r.riskPercent()).divide(HUNDRED,2, RoundingMode.HALF_UP);

        // 2. Brokerage is paid even if the trade works, so it eats into the risk budget.
        BigDecimal budget =riskAmount.subtract(r.brokerage());
        if(budget.signum()<=0){
            return rejected(SizingResult.Reason.BUDGET_TOO_SMALL,"Brokerage uses up the whole risk amount");
        }

        // 3. How much do I lose per share if the stop is hit?
        BigDecimal perShareRisk =r.entry().subtract(r.stop());

        // 4. Shares allowed by risk. Always round DOWN.
        long riskQty =budget.divide(perShareRisk,0,RoundingMode.DOWN).longValueExact();
        if(riskQty<1){
            return rejected(SizingResult.Reason.BUDGET_TOO_SMALL, "Risk budget is too small for even 1 share");
        }

        // 5. Shares I can actually afford with my capital.
        long affordableQty =r.capital().divide(r.entry(),0,RoundingMode.DOWN).longValueExact();
        if(affordableQty<1){
            return rejected(SizingResult.Reason.CANNOT_AFFORD, "Capital cannot buy even 1 share");
        }

        long qty=Math.min(riskQty,affordableQty);
        boolean capped=affordableQty<riskQty;
        BigDecimal positionValue =r.entry().multiply(BigDecimal.valueOf(qty));
        BigDecimal maxLoss =perShareRisk.multiply(BigDecimal.valueOf(qty)).add(r.brokerage());

        return  new SizingResult.Sized(qty,riskAmount,positionValue,maxLoss,capped);
    }

    private  Optional<SizingResult.Rejected> validate(SizingRequest r) {
        if (r.capital().signum() <= 0) {
            return Optional.of(rejected(SizingResult.Reason.INVALID_INPUT, "capital must be greater than 0"));
        }
        if (r.riskPercent().signum() <= 0) {
            return Optional.of(rejected(SizingResult.Reason.INVALID_INPUT, "riskPercent must be greater than 0"));
        }
        if (r.riskPercent().compareTo(maxRiskPercent) > 0) {
            return Optional.of(rejected(SizingResult.Reason.RISK_TOO_HIGH, "riskPercent is above the " + maxRiskPercent + "% limit"));
        }
        if (r.entry().signum() <= 0 || r.stop().signum() <= 0) {
            return Optional.of(rejected(SizingResult.Reason.INVALID_INPUT, "entry and stop must be greater than 0"));
        }
        if (r.brokerage().signum() < 0) {
            return Optional.of(rejected(SizingResult.Reason.INVALID_INPUT, "brokerage cannot be negative"));
        }
        if (r.stop().compareTo(r.entry()) >= 0) {
            return Optional.of(rejected(SizingResult.Reason.STOP_NOT_BELOW_ENTRY, "stop must be below entry (long trades only)"));
        }
        return Optional.empty();
    }

    private static SizingResult.Rejected rejected(SizingResult.Reason reason, String message) {
        return new SizingResult.Rejected(reason, message);
    }

    public static String explain(SizingResult result){
        return switch (result){
            case SizingResult.Sized s when s.cappedByCapital() ->"Buy %d shares (limited by your capital). Position value %s, maximum loss %s.".formatted(s.qty(),s.positionValue(),s.maxLoss());
            case SizingResult.Sized s->"Buy %d shares. Position value %s, maximum loss %s (risk budget %s).".formatted(s.qty(),s.positionValue(),s.maxLoss(),s.riskAmount());
            case SizingResult.Rejected r->"Rejected (" + r.reason() +") : "+r.message();
        };
    }
}
