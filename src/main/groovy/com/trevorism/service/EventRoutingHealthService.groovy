package com.trevorism.service

import com.trevorism.event.ChannelClient
import com.trevorism.event.DefaultChannelClient
import com.trevorism.event.model.EventSubscription
import com.trevorism.https.SecureHttpClient
import com.trevorism.model.HealthPanel

/**
 * Historical provider sourced from the event service's API (not a datastore kind): reports the
 * topic/subscription routing map. A topic with no subscription means published events go nowhere,
 * which is the routing anomaly worth flagging (amber).
 */
@jakarta.inject.Singleton
class EventRoutingHealthService extends PollingHealthProvider {

    private final ChannelClient channelClient

    EventRoutingHealthService(SecureHttpClient httpClient) {
        this.channelClient = new DefaultChannelClient(httpClient)
    }

    @Override
    String getKey() {
        return "events"
    }

    @Override
    String getTitle() {
        return "Event Routing"
    }

    @Override
    protected HealthPanel load() {
        List<String> topics = channelClient.listTopics()
        List<EventSubscription> subscriptions = channelClient.listSubscriptions()

        Set<String> subscribedTopics = subscriptions.collect { shortName(it.topic) } as Set
        List<String> unrouted = topics.findAll { !subscribedTopics.contains(shortName(it)) }

        String headline = "${topics.size()} topics · ${subscriptions.size()} subscriptions" +
                (unrouted ? " · ${unrouted.size()} unrouted" : '')

        return new HealthPanel(
                key: getKey(),
                title: getTitle(),
                status: unrouted ? HealthPanel.STATUS_WARN : HealthPanel.STATUS_OK,
                headline: headline,
                details: [topics: topics, subscriptions: subscriptions, unrouted: unrouted]
        )
    }

    // Topic names may come back qualified; compare on the trailing segment either way.
    private static String shortName(String topic) {
        return topic ? topic.tokenize('/').last() : topic
    }
}
