package com.trevorism.service

import com.trevorism.model.HealthPanel

/**
 * A pluggable source of health for one platform service. Implement this as a @Singleton bean
 * and it is automatically picked up by HealthController — no controller changes needed to add
 * a new tile to the dashboard.
 *
 * getHealth() is the single, unifying display contract: it always returns the current panel,
 * regardless of whether the data is pulled on demand (historical) or pushed via a callback
 * (real-time). Extend PollingHealthProvider or PushHealthProvider rather than implementing
 * this directly.
 */
interface HealthProvider {

    /** Stable identifier for this panel, used as the drill-down key (e.g. "testsuite"). */
    String getKey()

    /** Human-readable tile title (e.g. "Test Suites"). */
    String getTitle()

    /** The current at-a-glance summary (and, for drill-down, the details) for this service. */
    HealthPanel getHealth()

    /** When true, only admins may see this panel; non-admins never receive it. */
    default boolean adminOnly() {
        return false
    }
}
