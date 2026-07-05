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
