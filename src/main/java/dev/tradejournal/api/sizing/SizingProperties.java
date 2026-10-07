package dev.tradejournal.api.sizing;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "tradejournal.sizing")
public record SizingProperties(@DefaultValue("5") BigDecimal maxRiskPercent) {
}
