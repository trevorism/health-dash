package com.trevorism.controller

import com.trevorism.model.HealthPanel
import com.trevorism.secure.Roles
import com.trevorism.secure.Secure
import com.trevorism.service.HealthProvider
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag

@Controller("/api/health")
class HealthController {

    private final List<HealthProvider> providers

    HealthController(List<HealthProvider> providers) {
        this.providers = providers
    }

    @Tag(name = "Health Operations")
    @Operation(summary = "At-a-glance health for every registered service **Secure")
    @Secure(Roles.USER)
    @Get(produces = MediaType.APPLICATION_JSON)
    List<HealthPanel> allHealth() {
        return providers.collect { it.getHealth() }
    }

    @Tag(name = "Health Operations")
    @Operation(summary = "Health summary and drill-down detail for one service **Secure")
    @Secure(Roles.USER)
    @Get(value = "/{key}", produces = MediaType.APPLICATION_JSON)
    HttpResponse<HealthPanel> health(String key) {
        HealthProvider provider = providers.find { it.getKey() == key }
        if (!provider) {
            return HttpResponse.notFound()
        }
        return HttpResponse.ok(provider.getHealth())
    }
}
