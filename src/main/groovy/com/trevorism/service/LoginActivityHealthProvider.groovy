package com.trevorism.service

import com.trevorism.model.HealthPanel

import java.util.concurrent.ConcurrentLinkedDeque

/**
 * Real-time provider: the login service emits a LoginEvent{username, guid, success} to the
 * "login" topic on each sign-in. We subscribe to that topic (push), keep a rolling window of
 * recent logins in memory, and surface the latest activity — no polling of any datastore.
 */
@jakarta.inject.Singleton
class LoginActivityHealthProvider extends PushHealthProvider {

    private static final int WINDOW = 20
    private final Deque<Map> recent = new ConcurrentLinkedDeque<>()

    @Override
    String getKey() {
        return "logins"
    }

    @Override
    String getTitle() {
        return "Recent Logins"
    }

    @Override
    String getTopic() {
        return "login"
    }

    @Override
    protected HealthPanel reduce(HealthPanel current, Map event) {
        recent.addFirst(event)
        while (recent.size() > WINDOW) {
            recent.removeLast()
        }

        boolean lastFailed = !(event.success as boolean)
        int failures = recent.count { !(it.success as boolean) } as int
        String user = event.username ?: "unknown"

        return new HealthPanel(
                key: getKey(),
                title: getTitle(),
                status: lastFailed ? HealthPanel.STATUS_WARN : HealthPanel.STATUS_OK,
                headline: "last: ${user}${lastFailed ? ' (failed)' : ''} · ${failures} failed of last ${recent.size()}",
                details: [recent: recent.toList()]
        )
    }
}
