package com.trevorism.service

import com.trevorism.data.Repository
import com.trevorism.model.HealthPanel
import com.trevorism.model.TestSuite
import org.junit.jupiter.api.Test

class TestSuiteHealthServiceTest {

    private static TestSuiteHealthService serviceReturning(List<TestSuite> suites) {
        TestSuiteHealthService service = new TestSuiteHealthService(null)
        service.@testSuiteRepository = [list: { -> suites }] as Repository
        return service
    }

    @Test
    void testUnknownWhenNoSuites() {
        HealthPanel panel = serviceReturning([]).load()
        assert panel.status == HealthPanel.STATUS_UNKNOWN
        assert panel.headline == "0/0 suites passing"
    }

    @Test
    void testOkWhenAllPassing() {
        List<TestSuite> suites = [
                new TestSuite(name: "a", lastRunSuccess: true),
                new TestSuite(name: "b", lastRunSuccess: true)
        ]
        HealthPanel panel = serviceReturning(suites).load()
        assert panel.status == HealthPanel.STATUS_OK
        assert panel.headline == "2/2 suites passing"
    }

    @Test
    void testErrorWhenSomeFailingAndFailuresListedFirst() {
        List<TestSuite> suites = [
                new TestSuite(name: "passing", lastRunSuccess: true),
                new TestSuite(name: "failing", lastRunSuccess: false)
        ]
        HealthPanel panel = serviceReturning(suites).load()
        assert panel.status == HealthPanel.STATUS_ERROR
        assert panel.headline == "1/2 suites passing"
        assert panel.details.suites.first().name == "failing"
    }
}
