package com.trevorism.gcloud

/**
 * @author tbrooks
 */

this.metaClass.mixin(io.cucumber.groovy.Hooks)
this.metaClass.mixin(io.cucumber.groovy.EN)

String baseUrl = System.getenv("ACCEPTANCE_BASE_URL") ?: "https://health-dash.testing.trevorism.com"

def contextRootContent
def pingContent

Given(~/^the health-dash application is alive$/) { ->
    try {
        new URL("${baseUrl}/api/ping").text
    }
    catch (Exception ignored) {
        Thread.sleep(10000)
        new URL("${baseUrl}/api/ping").text
    }
}

When(~/^I navigate to "([^"]*)"$/) { String url ->
    contextRootContent = new URL("${baseUrl}/api").text
}

Then(~/^then a link to the help page is displayed$/) { ->
    assert contextRootContent
    assert contextRootContent.contains("/help")
}

When(~/^I ping the application deployed to "([^"]*)"$/) { String url ->
    pingContent = new URL("${baseUrl}/api/ping").text
}

Then(~/^pong is returned, to indicate the service is alive$/) { ->
    assert pingContent == "pong"
}
