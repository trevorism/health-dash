package com.trevorism.service

import com.trevorism.event.ChannelClient
import com.trevorism.event.model.EventSubscription
import com.trevorism.model.HealthPanel
import org.junit.jupiter.api.Test

import java.lang.reflect.Field

class HealthSubscriptionRegistrarTest {

    private static PushHealthProvider provider(String topic) {
        return new PushHealthProvider() {
            @Override String getKey() { topic }
            @Override String getTitle() { topic }
            @Override String getTopic() { topic }
            @Override protected HealthPanel reduce(HealthPanel current, Map event) { new HealthPanel() }
        }
    }

    private static HealthSubscriptionRegistrar registrar(List<PushHealthProvider> providers, ChannelClient channelClient) {
        HealthSubscriptionRegistrar registrar = new HealthSubscriptionRegistrar(providers)
        Field field = HealthSubscriptionRegistrar.getDeclaredField("channelClient")
        field.setAccessible(true)
        field.set(registrar, channelClient)
        return registrar
    }

    @Test
    void testCreatesSubscriptionWhenMissing() {
        List<EventSubscription> created = []
        ChannelClient channelClient = [
                getSubscription   : { String name -> throw new RuntimeException("404") },
                createSubscription: { EventSubscription subscription -> created << subscription; subscription }
        ] as ChannelClient

        registrar([provider("login")], channelClient).onStartup(null)

        assert created.size() == 1
        assert created[0].name == "health-dash-login"
        assert created[0].topic == "login"
        assert created[0].url == "https://health-dash.testing.trevorism.com/api/health/event/login"
    }

    @Test
    void testSkipsCreationWhenSubscriptionAlreadyExists() {
        List<EventSubscription> created = []
        ChannelClient channelClient = [
                getSubscription   : { String name -> new EventSubscription(name: name) },
                createSubscription: { EventSubscription subscription -> created << subscription; subscription }
        ] as ChannelClient

        registrar([provider("login")], channelClient).onStartup(null)

        assert created.isEmpty()
    }

    @Test
    void testOneFailingProviderDoesNotBlockTheRest() {
        List<EventSubscription> created = []
        ChannelClient channelClient = [
                getSubscription   : { String name -> throw new RuntimeException("404") },
                createSubscription: { EventSubscription subscription ->
                    if (subscription.topic == "broken") {
                        throw new RuntimeException("boom")
                    }
                    created << subscription
                    subscription
                }
        ] as ChannelClient

        registrar([provider("broken"), provider("login")], channelClient).onStartup(null)

        assert created.size() == 1
        assert created[0].topic == "login"
    }
}
