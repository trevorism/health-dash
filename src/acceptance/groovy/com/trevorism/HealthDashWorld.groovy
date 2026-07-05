package com.trevorism

import com.google.gson.Gson
import com.trevorism.http.HttpClient
import com.trevorism.http.JsonHttpClient
import com.trevorism.https.AppClientSecureHttpClient
import com.trevorism.https.SecureHttpClient

/**
 * Shared state and HTTP helpers for the health-dash acceptance suite, run against the deployed
 * instance. Authenticated calls use the app identity (AppClientSecureHttpClient); anonymous
 * calls use a plain JsonHttpClient, which throws on a non-2xx response, so a secured endpoint
 * rejecting an unauthenticated caller surfaces as rejected == true.
 */
class HealthDashWorld {

    static final String BASE_URL = "https://health-dash.testing.trevorism.com"

    private final Gson gson = new Gson()
    private final SecureHttpClient authClient = new AppClientSecureHttpClient()
    private final HttpClient anonClient = new JsonHttpClient()

    String body
    boolean rejected
    List panels
    Map panel

    List requestAllPanels() {
        body = authClient.get("${BASE_URL}/api/health".toString())
        panels = gson.fromJson(body, List)
        return panels
    }

    Map requestPanel(String key) {
        body = authClient.get("${BASE_URL}/api/health/${key}".toString())
        panel = gson.fromJson(body, Map)
        return panel
    }

    void anonGet(String path) {
        try {
            body = anonClient.get("${BASE_URL}/${path}".toString())
            rejected = false
        } catch (Exception ignored) {
            rejected = true
            body = null
        }
    }

    void anonPost(String path) {
        try {
            body = anonClient.post("${BASE_URL}/${path}".toString(), "{}")
            rejected = false
        } catch (Exception ignored) {
            rejected = true
            body = null
        }
    }
}
