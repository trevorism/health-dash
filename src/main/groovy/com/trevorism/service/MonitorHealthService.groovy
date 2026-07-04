package com.trevorism.service

import com.trevorism.data.FastDatastoreRepository
import com.trevorism.data.Repository
import com.trevorism.https.SecureHttpClient
import com.trevorism.model.HealthPanel
import com.trevorism.model.Monitor

/**
 * Historical provider: reads the Monitor datastore kind and reports monitoring coverage.
 * Monitors are scheduled test runs, so "how much is being watched" is the health signal.
 */
@jakarta.inject.Singleton
class MonitorHealthService extends PollingHealthProvider {

    private Repository<Monitor> monitorRepository

    MonitorHealthService(SecureHttpClient httpClient) {
        monitorRepository = new FastDatastoreRepository<>(Monitor, httpClient)
    }

    @Override
    String getKey() {
        return "monitors"
    }

    @Override
    String getTitle() {
        return "Monitors"
    }

    @Override
    protected HealthPanel load() {
        List<Monitor> monitors = monitorRepository.list()

        int total = monitors.size()
        int sources = monitors.collect { it.source }.unique().size()

        return new HealthPanel(
                key: getKey(),
                title: getTitle(),
                status: total == 0 ? HealthPanel.STATUS_WARN : HealthPanel.STATUS_OK,
                headline: "${total} monitors across ${sources} sources",
                details: [monitors: monitors]
        )
    }
}
