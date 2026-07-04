package com.trevorism.controller

import com.trevorism.model.HealthPanel
import com.trevorism.service.PushHealthProvider
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import org.junit.jupiter.api.Test

class HealthEventControllerTest {

    private static class RecordingProvider extends PushHealthProvider {
        String topic
        Map lastEvent

        RecordingProvider(String topic) { this.topic = topic }

        @Override String getKey() { topic }
        @Override String getTitle() { topic }
        @Override String getTopic() { topic }
        @Override protected HealthPanel reduce(HealthPanel current, Map event) {
            lastEvent = event
            new HealthPanel(key: topic)
        }
    }

    @Test
    void testIngestRoutesEventToProviderForTopic() {
        RecordingProvider login = new RecordingProvider("login")
        HealthEventController controller = new HealthEventController([login])

        HttpResponse response = controller.ingest("login", [username: "bob", success: true])

        assert response.status() == HttpStatus.OK
        assert login.lastEvent == [username: "bob", success: true]
    }

    @Test
    void testIngestReturnsNotFoundForUnknownTopic() {
        RecordingProvider login = new RecordingProvider("login")
        HealthEventController controller = new HealthEventController([login])

        HttpResponse response = controller.ingest("unknown", [foo: "bar"])

        assert response.status() == HttpStatus.NOT_FOUND
        assert login.lastEvent == null
    }
}
