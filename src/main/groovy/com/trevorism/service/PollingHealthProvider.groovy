package com.trevorism.service

import com.trevorism.model.HealthPanel
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Base for HISTORICAL providers: the panel is built by looking up a data source on demand
 * (datastore, bigquery, another service's API) each time getHealth() is called.
 *
 * Subclasses implement load(); this base handles error fallback and stamps the panel with
 * its mode + freshness so all tiles display uniformly.
 */
abstract class PollingHealthProvider implements HealthProvider {

    private static final Logger log = LoggerFactory.getLogger(PollingHealthProvider)

    @Override
    HealthPanel getHealth() {
        HealthPanel panel
        try {
            panel = load()
        } catch (Exception e) {
            log.error("Unable to load health for ${getKey()}", e)
            panel = new HealthPanel(key: getKey(), title: getTitle(), status: HealthPanel.STATUS_UNKNOWN, headline: "Unavailable")
        }
        panel.mode = HealthPanel.MODE_HISTORICAL
        panel.lastUpdated = new Date()
        return panel
    }

    /** Query the underlying source and build the panel. May throw; the base converts to UNKNOWN. */
    protected abstract HealthPanel load()
}
