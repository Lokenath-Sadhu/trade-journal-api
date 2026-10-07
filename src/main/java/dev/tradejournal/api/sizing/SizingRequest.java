package dev.tradejournal.api.sizing;

import java.math.BigDecimal;
import java.util.Objects;


public record SizingRequest(BigDecimal capital,BigDecimal riskPercent,BigDecimal entry,BigDecimal stop,BigDecimal brokerage){
public SizingRequest{
    Objects.requireNonNull(capital,"capital");
    Objects.requireNonNull(riskPercent,"riskPercent");
    Objects.requireNonNull(entry,"entry");
    Objects.requireNonNull(stop,"stop");
    Objects.requireNonNull(brokerage,"brokerage");

}
    }

