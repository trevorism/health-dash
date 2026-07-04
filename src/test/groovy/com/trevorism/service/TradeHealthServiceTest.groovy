package com.trevorism.service

import com.trevorism.https.SecureHttpClient
import com.trevorism.model.HealthPanel
import org.junit.jupiter.api.Test

class TradeHealthServiceTest {

    private static final String ORDERS = "https://trade.trevorism.com/trade/"
    private static final String BALANCE = "https://trade.trevorism.com/balance/"
    private static final String TOTAL = "https://trade.trevorism.com/balance/total/USD"

    private static TradeHealthService serviceWith(Map<String, String> responses) {
        TradeHealthService service = new TradeHealthService()
        SecureHttpClient client = [get: { String url -> responses[url] }] as SecureHttpClient
        // httpClient is a final field, so reflection is needed to swap in the stub.
        java.lang.reflect.Field field = TradeHealthService.getDeclaredField("httpClient")
        field.setAccessible(true)
        field.set(service, client)
        return service
    }

    @Test
    void testAdminOnly() {
        assert new TradeHealthService().adminOnly()
    }

    @Test
    void testHeadlineIncludesOrderCountAndBalance() {
        TradeHealthService service = serviceWith([(ORDERS): "[{},{}]", (BALANCE): "[{}]", (TOTAL): "1234.5"])
        HealthPanel panel = service.load()
        assert panel.status == HealthPanel.STATUS_OK
        assert panel.headline == "2 open orders · \$1234.50"
        assert panel.details.orders.size() == 2
        assert panel.details.balances.size() == 1
    }

    @Test
    void testBalanceOmittedWhenTotalUnparseable() {
        TradeHealthService service = serviceWith([(ORDERS): "[]", (BALANCE): "[]", (TOTAL): "not-a-number"])
        HealthPanel panel = service.load()
        assert panel.headline == "0 open orders"
    }

    @Test
    void testEmptyResponsesTreatedAsEmpty() {
        TradeHealthService service = serviceWith([(ORDERS): "", (BALANCE): "", (TOTAL): ""])
        HealthPanel panel = service.load()
        // empty JSON parses to null and falls back to []; total falls back to "0", so balance still renders
        assert panel.headline == "0 open orders · \$0.00"
        assert panel.details.orders == []
    }
}
