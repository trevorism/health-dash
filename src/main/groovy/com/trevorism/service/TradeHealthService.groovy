package com.trevorism.service

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.trevorism.https.AppClientSecureHttpClient
import com.trevorism.https.SecureHttpClient
import com.trevorism.model.HealthPanel

import java.lang.reflect.Type

/**
 * Admin-only provider sourced from the trade service's API. Trade endpoints require the SYSTEM
 * role, so this uses the app identity (AppClientSecureHttpClient) rather than the caller's token.
 * Non-admins never receive this panel (HealthController filters it out).
 */
@jakarta.inject.Singleton
class TradeHealthService extends PollingHealthProvider {

    private static final String ORDERS_URL = "https://trade.trevorism.com/trade/"
    private static final String BALANCE_URL = "https://trade.trevorism.com/balance/"
    private static final String TOTAL_URL = "https://trade.trevorism.com/balance/total/USD"
    private static final Type LIST_TYPE = new TypeToken<List<Map>>() {}.getType()

    private final SecureHttpClient httpClient = new AppClientSecureHttpClient()
    private final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create()

    @Override
    boolean adminOnly() {
        return true
    }

    @Override
    String getKey() {
        return "trades"
    }

    @Override
    String getTitle() {
        return "Trades"
    }

    @Override
    protected HealthPanel load() {
        List<Map> orders = gson.fromJson(httpClient.get(ORDERS_URL), LIST_TYPE) ?: []
        List<Map> balances = gson.fromJson(httpClient.get(BALANCE_URL), LIST_TYPE) ?: []

        // Account value in USD; guarded so a total hiccup doesn't blank the whole card.
        String balanceText = ""
        try {
            double total = (httpClient.get(TOTAL_URL) ?: "0") as double
            balanceText = " · \$${String.format('%.2f', total)}"
        } catch (Exception ignored) {
        }

        return new HealthPanel(
                key: getKey(),
                title: getTitle(),
                status: HealthPanel.STATUS_OK,
                headline: "${orders.size()} open orders${balanceText}",
                details: [orders: orders, balances: balances]
        )
    }
}
