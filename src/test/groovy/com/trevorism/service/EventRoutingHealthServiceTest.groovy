package com.trevorism.service

import com.trevorism.event.ChannelClient
import com.trevorism.event.model.EventSubscription
import com.trevorism.model.HealthPanel
import org.junit.jupiter.api.Test

class EventRoutingHealthServiceTest {

    private static EventRoutingHealthService service(List<String> topics, List<EventSubscription> subs) {
        EventRoutingHealthService service = new EventRoutingHealthService(null)
        ChannelClient client = [listTopics: { -> topics }, listSubscriptions: { -> subs }] as ChannelClient
        // channelClient is a final field, so reflection is needed to swap in the stub.
        java.lang.reflect.Field field = EventRoutingHealthService.getDeclaredField("channelClient")
        field.setAccessible(true)
        field.set(service, client)
        return service
    }

    private static EventSubscription sub(String topic) {
        EventSubscription s = new EventSubscription()
        s.setTopic(topic)
        return s
    }

    @Test
    void testOkWhenEveryTopicSubscribed() {
        HealthPanel panel = service(["login", "trade"], [sub("login"), sub("trade")]).load()
        assert panel.status == HealthPanel.STATUS_OK
        assert panel.headline == "2 topics · 2 subscriptions"
        assert panel.details.unrouted == []
    }

    @Test
    void testWarnWhenTopicHasNoSubscription() {
        HealthPanel panel = service(["login", "trade"], [sub("login")]).load()
        assert panel.status == HealthPanel.STATUS_WARN
        assert panel.headline == "2 topics · 1 subscriptions · 1 unrouted"
        assert panel.details.unrouted == ["trade"]
    }

    @Test
    void testMatchesTopicsByShortNameIgnoringQualifiers() {
        HealthPanel panel = service(["login"], [sub("projects/foo/topics/login")]).load()
        assert panel.status == HealthPanel.STATUS_OK
        assert panel.details.unrouted == []
    }
}
