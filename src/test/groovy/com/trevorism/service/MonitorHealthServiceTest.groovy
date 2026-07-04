package com.trevorism.service

import com.trevorism.data.Repository
import com.trevorism.model.HealthPanel
import com.trevorism.model.Monitor
import org.junit.jupiter.api.Test

class MonitorHealthServiceTest {

    private static MonitorHealthService serviceReturning(List<Monitor> monitors) {
        MonitorHealthService service = new MonitorHealthService(null)
        service.@monitorRepository = [list: { -> monitors }] as Repository
        return service
    }

    @Test
    void testKeyAndTitle() {
        MonitorHealthService service = serviceReturning([])
        assert service.getKey() == "monitors"
        assert service.getTitle() == "Monitors"
    }

    @Test
    void testWarnWhenNoMonitors() {
        HealthPanel panel = serviceReturning([]).load()
        assert panel.status == HealthPanel.STATUS_WARN
        assert panel.headline == "0 monitors across 0 sources"
    }

    @Test
    void testOkAndCountsUniqueSources() {
        List<Monitor> monitors = [
                new Monitor(source: "trade"),
                new Monitor(source: "trade"),
                new Monitor(source: "login")
        ]
        HealthPanel panel = serviceReturning(monitors).load()
        assert panel.status == HealthPanel.STATUS_OK
        assert panel.headline == "3 monitors across 2 sources"
        assert panel.details.monitors.size() == 3
    }
}
