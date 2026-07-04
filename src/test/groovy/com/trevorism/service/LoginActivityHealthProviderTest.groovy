package com.trevorism.service

import com.trevorism.model.HealthPanel
import org.junit.jupiter.api.Test

class LoginActivityHealthProviderTest {

    @Test
    void testKeyTitleTopic() {
        LoginActivityHealthProvider p = new LoginActivityHealthProvider()
        assert p.getKey() == "logins"
        assert p.getTitle() == "Recent Logins"
        assert p.getTopic() == "login"
    }

    @Test
    void testSuccessfulLoginIsOk() {
        LoginActivityHealthProvider p = new LoginActivityHealthProvider()
        p.ingest([username: "alice", success: true])
        HealthPanel panel = p.getHealth()
        assert panel.status == HealthPanel.STATUS_OK
        assert panel.headline == "last: alice · 0 failed of last 1"
    }

    @Test
    void testFailedLoginIsWarnAndCountsFailures() {
        LoginActivityHealthProvider p = new LoginActivityHealthProvider()
        p.ingest([username: "alice", success: true])
        p.ingest([username: "bob", success: false])
        HealthPanel panel = p.getHealth()
        assert panel.status == HealthPanel.STATUS_WARN
        assert panel.headline == "last: bob (failed) · 1 failed of last 2"
    }

    @Test
    void testUnknownUsernameFallback() {
        LoginActivityHealthProvider p = new LoginActivityHealthProvider()
        p.ingest([success: true])
        assert p.getHealth().headline == "last: unknown · 0 failed of last 1"
    }

    @Test
    void testWindowIsCappedAtTwenty() {
        LoginActivityHealthProvider p = new LoginActivityHealthProvider()
        (1..25).each { p.ingest([username: "u${it}", success: true]) }
        HealthPanel panel = p.getHealth()
        assert panel.details.recent.size() == 20
        assert panel.headline == "last: u25 · 0 failed of last 20"
    }
}
