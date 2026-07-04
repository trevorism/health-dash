package com.trevorism.service

import com.trevorism.event.ChannelClient
import com.trevorism.event.DefaultChannelClient
import com.trevorism.event.model.EventSubscription
import com.trevorism.https.AppClientSecureHttpClient
import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.runtime.server.event.ServerStartupEvent
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * On startup, registers a push subscription for every real-time provider so the event service
 * delivers that topic's events to our webhook (HealthEventController). Best-effort: a failure
 * (e.g. the subscription already exists) is logged and ignored.
 */
@Singleton
class HealthSubscriptionRegistrar {

    private static final Logger log = LoggerFactory.getLogger(HealthSubscriptionRegistrar)
    private static final String BASE_URL = "https://health-dash.testing.trevorism.com"

    private final ChannelClient channelClient = new DefaultChannelClient(new AppClientSecureHttpClient())
    private final List<PushHealthProvider> providers

    HealthSubscriptionRegistrar(List<PushHealthProvider> providers) {
        this.providers = providers
    }

    @EventListener
    void onStartup(ServerStartupEvent event) {
        providers.each { provider ->
            try {
                EventSubscription subscription = new EventSubscription()
                subscription.name = "health-dash-${provider.getTopic()}"
                subscription.topic = provider.getTopic()
                subscription.url = "${BASE_URL}/api/health/event/${provider.getTopic()}"
                channelClient.createSubscription(subscription)
                log.info("Registered health-dash subscription for topic '${provider.getTopic()}'")
            } catch (Exception e) {
                log.warn("Could not register subscription for topic '${provider.getTopic()}' (may already exist): ${e.message}")
            }
        }
    }
}
