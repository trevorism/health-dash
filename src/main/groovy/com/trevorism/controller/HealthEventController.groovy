package com.trevorism.controller

import com.trevorism.service.PushHealthProvider
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Push endpoint for real-time providers. The event service (GCP Pub/Sub push, unwrapped) POSTs
 * the raw event JSON here; we route it to the PushHealthProvider registered for that topic.
 *
 * Intentionally unsecured — Pub/Sub push authenticates with a Google service account, not a
 * trevorism user token (same convention as other subscriber webhooks on the platform).
 */
@Controller("/api/health/event")
class HealthEventController {

    private static final Logger log = LoggerFactory.getLogger(HealthEventController)

    private final Map<String, PushHealthProvider> providersByTopic

    HealthEventController(List<PushHealthProvider> providers) {
        this.providersByTopic = providers.collectEntries { [(it.getTopic()): it] }
    }

    @Tag(name = "Health Operations")
    @Operation(summary = "Ingest a real-time event for the given topic")
    @Post(value = "/{topic}", consumes = MediaType.APPLICATION_JSON)
    HttpResponse ingest(String topic, @Body Map event) {
        PushHealthProvider provider = providersByTopic[topic]
        if (!provider) {
            log.warn("No real-time provider registered for topic '${topic}'")
            return HttpResponse.notFound()
        }
        provider.ingest(event)
        return HttpResponse.ok()
    }
}
