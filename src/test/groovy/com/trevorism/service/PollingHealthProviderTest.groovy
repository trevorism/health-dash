package com.trevorism.service

import com.trevorism.model.HealthPanel
import org.junit.jupiter.api.Test

class PollingHealthProviderTest {

    private static PollingHealthProvider provider(String key, String title, Closure<HealthPanel> loader) {
        return new PollingHealthProvider() {
            @Override String getKey() { key }
            @Override String getTitle() { title }
            @Override protected HealthPanel load() { loader() }
        }
    }

    @Test
    void testGetHealthStampsHistoricalModeAndTimestamp() {
        HealthPanel built = new HealthPanel(key: "k", title: "T", status: HealthPanel.STATUS_OK, headline: "fine")
        PollingHealthProvider p = provider("k", "T", { built })

        HealthPanel result = p.getHealth()

        assert result.is(built)
        assert result.status == HealthPanel.STATUS_OK
        assert result.mode == HealthPanel.MODE_HISTORICAL
        assert result.lastUpdated != null
    }

    @Test
    void testGetHealthFallsBackToUnknownWhenLoadThrows() {
        PollingHealthProvider p = provider("widgets", "Widgets", { throw new RuntimeException("boom") })

        HealthPanel result = p.getHealth()

        assert result.key == "widgets"
        assert result.title == "Widgets"
        assert result.status == HealthPanel.STATUS_UNKNOWN
        assert result.headline == "Unavailable"
        assert result.mode == HealthPanel.MODE_HISTORICAL
        assert result.lastUpdated != null
    }

    @Test
    void testAdminOnlyDefaultsToFalse() {
        assert !provider("k", "T", { new HealthPanel() }).adminOnly()
    }
}
