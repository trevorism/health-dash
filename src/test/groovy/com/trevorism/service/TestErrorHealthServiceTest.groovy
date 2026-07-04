package com.trevorism.service

import com.trevorism.data.Repository
import com.trevorism.data.model.sorting.Sort
import com.trevorism.model.HealthPanel
import com.trevorism.model.TestError
import org.junit.jupiter.api.Test

class TestErrorHealthServiceTest {

    private static TestErrorHealthService serviceReturning(List<TestError> errors) {
        TestErrorHealthService service = new TestErrorHealthService(null)
        service.@errorRepository = [sort: { Sort s -> errors }] as Repository
        return service
    }

    @Test
    void testOkWhenNoErrors() {
        HealthPanel panel = serviceReturning([]).load()
        assert panel.status == HealthPanel.STATUS_OK
        assert panel.headline == "No active errors"
        assert panel.details.errors == []
    }

    @Test
    void testErrorWhenErrorsPresentWithLatestSource() {
        List<TestError> errors = [
                new TestError(source: "trade", message: "x"),
                new TestError(source: "login", message: "y")
        ]
        HealthPanel panel = serviceReturning(errors).load()
        assert panel.status == HealthPanel.STATUS_ERROR
        assert panel.headline == "2 unresolved · latest: trade"
    }

    @Test
    void testDetailsCappedAtRecentLimit() {
        List<TestError> errors = (1..30).collect { new TestError(source: "s${it}") }
        HealthPanel panel = serviceReturning(errors).load()
        assert panel.details.errors.size() == 20
    }
}
