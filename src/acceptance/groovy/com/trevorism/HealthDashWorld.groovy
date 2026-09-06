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

    static final String BASE_URL = System.getenv("ACCEPTANCE_BASE_URL") ?: "https://health-dash.testing.trevorism.com"

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

    static final int CONNECT_TIMEOUT_MS = 10_000
    static final int READ_TIMEOUT_MS = 30_000

    int status
    String location

    private HttpURLConnection open(String path, String method) {
        HttpURLConnection connection = new URL("${BASE_URL}/${path}").openConnection() as HttpURLConnection
        connection.instanceFollowRedirects = false
        connection.requestMethod = method
        connection.connectTimeout = CONNECT_TIMEOUT_MS
        connection.readTimeout = READ_TIMEOUT_MS
        return connection
    }

    /**
     * Posts with no body under a caller-chosen content type. A browser labels a bodyless post
     * form-urlencoded, and an endpoint that only consumes JSON answers 415 before the handler
     * runs, which is invisible to a suite that always sends JSON.
     */
    void anonPostAs(String path, String contentType) {
        HttpURLConnection connection = open(path, "POST")
        connection.setRequestProperty("Content-Type", contentType)
        connection.setRequestProperty("Content-Length", "0")
        connection.doOutput = true
        connection.outputStream.withCloseable { it.write(new byte[0]) }
        status = connection.responseCode
        body = status < 400 ? connection.inputStream.text : null
        connection.disconnect()
    }

    void anonGetWithoutFollowing(String path) {
        HttpURLConnection connection = open(path, "GET")
        status = connection.responseCode
        location = connection.getHeaderField("Location")
        connection.disconnect()
    }
}
