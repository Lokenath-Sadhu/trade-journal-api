package dev.tradejournal.api.sizing;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/sizing")
public class SizingController {
    private final PositionSizer sizer;

    public SizingController(PositionSizer sizer) { // Spring injects the bean
        this.sizer = sizer;
    }

    @GetMapping
    public SizingResult size(@RequestParam BigDecimal capital,
                        @RequestParam BigDecimal riskPercent,
                        @RequestParam BigDecimal entry,
                        @RequestParam BigDecimal stop,
                        @RequestParam(defaultValue = "0") BigDecimal brokerage){
        return sizer.size(new SizingRequest(capital, riskPercent, entry, stop, brokerage));
    }
}
