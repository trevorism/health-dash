package com.trevorism.controller

import com.trevorism.model.HealthPanel
import com.trevorism.secure.Roles
import com.trevorism.secure.Secure
import com.trevorism.service.HealthProvider
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.security.authentication.Authentication
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
    List<HealthPanel> allHealth(Authentication authentication) {
        boolean admin = isAdmin(authentication)
        return providers.findAll { !it.adminOnly() || admin }.collect { it.getHealth() }
    }

    @Tag(name = "Health Operations")
    @Operation(summary = "Health summary and drill-down detail for one service **Secure")
    @Secure(Roles.USER)
    @Get(value = "/{key}", produces = MediaType.APPLICATION_JSON)
    HttpResponse<HealthPanel> health(String key, Authentication authentication) {
        HealthProvider provider = providers.find { it.getKey() == key }
        // 404 (not 403) for admin-only panels a non-admin can't see, so we don't reveal existence.
        if (!provider || (provider.adminOnly() && !isAdmin(authentication))) {
            return HttpResponse.notFound()
        }
        return HttpResponse.ok(provider.getHealth())
    }

    private static boolean isAdmin(Authentication authentication) {
        return authentication?.getRoles()?.contains(Roles.ADMIN)
    }
}
