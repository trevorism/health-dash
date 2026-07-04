package com.trevorism.service

import com.trevorism.model.HealthPanel
import org.junit.jupiter.api.Test

class PushHealthProviderTest {

    private static PushHealthProvider provider(Closure<HealthPanel> reducer) {
        return new PushHealthProvider() {
            @Override String getKey() { "k" }
            @Override String getTitle() { "T" }
            @Override String getTopic() { "topic" }
            @Override protected HealthPanel reduce(HealthPanel current, Map event) { reducer(current, event) }
        }
    }

    @Test
    void testGetHealthReturnsAwaitingDataBeforeAnyEvent() {
        PushHealthProvider p = provider { c, e -> new HealthPanel() }

        HealthPanel result = p.getHealth()

        assert result.key == "k"
        assert result.title == "T"
        assert result.status == HealthPanel.STATUS_UNKNOWN
        assert result.headline == "Awaiting data"
        assert result.mode == HealthPanel.MODE_REALTIME
    }

    @Test
    void testIngestFoldsEventAndStampsRealtime() {
        PushHealthProvider p = provider { HealthPanel current, Map event ->
            new HealthPanel(key: "k", title: "T", status: HealthPanel.STATUS_OK, headline: "saw ${event.value}")
        }

        p.ingest([value: 42])

        HealthPanel result = p.getHealth()
        assert result.headline == "saw 42"
        assert result.mode == HealthPanel.MODE_REALTIME
        assert result.lastUpdated != null
    }

    @Test
    void testReduceReceivesNullFirstThenPreviousPanel() {
        List<HealthPanel> seen = []
        PushHealthProvider p = provider { HealthPanel current, Map event ->
            seen << current
            new HealthPanel(headline: "count ${event.n}")
        }

        p.ingest([n: 1])
        p.ingest([n: 2])

        assert seen[0] == null
        assert seen[1] != null
        assert seen[1].headline == "count 1"
    }
}
