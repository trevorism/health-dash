package com.trevorism.gcloud

import com.trevorism.HealthDashWorld

this.metaClass.mixin(io.cucumber.groovy.Hooks)
this.metaClass.mixin(io.cucumber.groovy.EN)

World {
    new HealthDashWorld()
}

When(~/^I GET "(.*)" anonymously$/) { String path ->
    anonGet(path)
}

When(~/^I POST "(.*)" anonymously$/) { String path ->
    anonPost(path)
}

Then(~/^the response body is "(.*)"$/) { String expected ->
    assert body?.trim() == expected
}

Then(~/^the request is rejected$/) { ->
    assert rejected
}

When(~/^I POST "(.*)" as "(.*)"$/) { String path, String contentType ->
    anonPostAs(path, contentType)
}

When(~/^I GET "(.*)" without following redirects$/) { String path ->
    anonGetWithoutFollowing(path)
}

Then(~/^the response status is (\d+)$/) { Integer expected ->
    assert status == expected, "expected ${expected} but got ${status}"
}

Then(~/^the response body reports nobody is signed in$/) { ->
    assert body?.contains('"authenticated":false'), "unexpected session body: ${body}"
}

Then(~/^the response redirects to the login application with a callback on this host$/) { ->
    assert status == 302, "expected a 302 but got ${status}"
    assert location?.startsWith("https://login.auth.trevorism.com/authorize"), location
    assert location.contains(URLEncoder.encode("${HealthDashWorld.BASE_URL}/api/auth/callback", "UTF-8")), location
    assert location.contains("state="), location
}

When(~/^I request all health panels as an authenticated user$/) { ->
    requestAllPanels()
}

Then(~/^a health panel for "(.*)" is returned$/) { String key ->
    assert panels.find { it.key == key }, "expected a panel with key '${key}' in ${panels*.key}"
}

Then(~/^every panel reports a status and a headline$/) { ->
    assert !panels.isEmpty()
    panels.each { Map p ->
        assert p.status, "panel ${p.key} is missing a status"
        assert p.headline != null, "panel ${p.key} is missing a headline"
    }
}

When(~/^I request the "(.*)" health panel as an authenticated user$/) { String key ->
    requestPanel(key)
}

Then(~/^the returned panel has key "(.*)"$/) { String key ->
    assert panel.key == key
}
