package dev.tradejournal.api.sizing;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class PositionSizerTest {
    private final PositionSizer sizer = new PositionSizer();

    private static SizingRequest req(String capital, String riskPct, String entry,
                                     String stop, String brokerage) {
        return new SizingRequest(new BigDecimal(capital), new BigDecimal(riskPct), new BigDecimal(entry), new BigDecimal(stop), new BigDecimal(brokerage));
    }

    private static void assertMoney(String actual, BigDecimal expected) {
        assertEquals(0, new BigDecimal(actual).compareTo(expected), "expected " + expected + " actual " + actual);
    }

    private void assertRejected(SizingResult.Reason expected, SizingRequest request) {
        var rejected = assertInstanceOf(SizingResult.Rejected.class, sizer.size(request));
        assertEquals(expected, rejected.reason());
    }

    @Test
    void sizesByRiskAfterBrokerage(){
        var sized=assertInstanceOf(SizingResult.Sized.class,sizer.size(req("500000","1","1500","1450","40")));
        assertEquals(99,sized.qty());
        assertMoney("5000",sized.riskAmount());
        assertMoney("148500",sized.positionValue());
        assertMoney("4990", sized.maxLoss());
        assertFalse(sized.cappedByCapital());
    }

    @Test
    void limitsQtyThatCapitalCanBuy(){
        var sized=assertInstanceOf(SizingResult.Sized.class,sizer.size(req("50000","2","1000","999","0")));
        assertEquals(50,sized.qty());
        assertMoney("1000",sized.riskAmount());
        assertMoney("50000",sized.positionValue());
        assertTrue(sized.cappedByCapital());
    }

    @Test
    void roundsQtyDownSoLossNeverExceedsRiskAmount(){
        var sized=assertInstanceOf(SizingResult.Sized.class,sizer.size(req("100000", "1", "100", "97", "0")));
        assertEquals(333,sized.qty());
        assertTrue(sized.maxLoss().compareTo(sized.riskAmount())<=0);
    }

    @Test
    void rejectsStopAboveEntry() {
        assertRejected(SizingResult.Reason.STOP_NOT_BELOW_ENTRY,req("500000","1","1500","1550","40"));
    }

    @Test
    void rejectsStopEqualToEntry(){
       assertRejected(SizingResult.Reason.STOP_NOT_BELOW_ENTRY,req("500000","1","1500","1500","40"));
    }

    @Test
    void rejectsRiskPercentAboveLimit(){
        assertRejected(SizingResult.Reason.RISK_TOO_HIGH,req("500000","6","1500","1500","40"));
    }

    @Test
    void rejectsWhenBrokerageEatsTheWholeRiskAmount(){
        assertRejected(SizingResult.Reason.BUDGET_TOO_SMALL, req("10000", "1", "1000", "800", "20"));
    }

    @Test
    void rejectsWhenCapitalCannotBuyOneShare(){
        assertRejected(SizingResult.Reason.CANNOT_AFFORD,req("500", "5", "1000", "999", "0"));
    }

    @Test
    void missingFieldsAreNotAllowed(){
        assertThrows(NullPointerException.class,()->new SizingRequest(null,BigDecimal.ONE,BigDecimal.TEN,BigDecimal.ONE,BigDecimal.ZERO));
    }

    @Test
    void explainsASizedResultInWords(){
        var text=sizer.explain(sizer.size(req("500000", "1", "1500", "1450", "40")));
        assertTrue(text.startsWith("Buy 99 shares"));
    }

    @Test
    void negativeBrokerageIsNotAllowed(){
        assertRejected(SizingResult.Reason.INVALID_INPUT,req("500000", "1", "1500", "1450", "-40"));
    }

    @Test
    void alllowRiskPer5(){
        assertInstanceOf(SizingResult.Sized.class,sizer.size(req("500000","5","1500","1450","40")));
    }

}
