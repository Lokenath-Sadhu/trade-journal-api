package dev.tradejournal.api.sizing;

import java.math.BigDecimal;

public sealed interface SizingResult permits SizingResult.Sized , SizingResult.Rejected{
    enum Reason { INVALID_INPUT, RISK_TOO_HIGH, STOP_NOT_BELOW_ENTRY, BUDGET_TOO_SMALL, CANNOT_AFFORD }
    record Sized(long qty, BigDecimal riskAmount,BigDecimal positionValue,BigDecimal maxLoss, boolean cappedByCapital) implements SizingResult {}
    record Rejected(Reason reason, String message) implements SizingResult{}
}
