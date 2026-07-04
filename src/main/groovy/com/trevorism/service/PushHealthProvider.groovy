package com.trevorism.service

import com.trevorism.model.HealthPanel

/**
 * Base for REAL-TIME providers: the panel is not fetched on read. Instead the latest snapshot
 * is held in memory and refreshed by a callback (ingest) whenever new data arrives — typically
 * an event subscription push or a webhook. getHealth() simply returns that current snapshot,
 * so the display contract is identical to a historical provider.
 *
 * A dispatcher (event/webhook controller) resolves the target provider and calls ingest(event).
 */
abstract class PushHealthProvider implements HealthProvider {

    protected volatile HealthPanel latest

    /** The event topic this provider subscribes to; the dispatcher routes events by topic. */
    abstract String getTopic()

    @Override
    HealthPanel getHealth() {
        if (latest == null) {
            return new HealthPanel(key: getKey(), title: getTitle(), status: HealthPanel.STATUS_UNKNOWN,
                    headline: "Awaiting data", mode: HealthPanel.MODE_REALTIME)
        }
        return latest
    }

    /** Callback invoked when a new real-time event arrives for this provider. */
    void ingest(Map event) {
        HealthPanel panel = reduce(latest, event)
        panel.mode = HealthPanel.MODE_REALTIME
        panel.lastUpdated = new Date()
        this.latest = panel
    }

    /** Fold an incoming event into the current panel (current may be null on first event). */
    protected abstract HealthPanel reduce(HealthPanel current, Map event)
}
