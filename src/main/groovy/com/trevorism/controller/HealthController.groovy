package com.trevorism.controller

import com.trevorism.model.HealthPanel
import com.trevorism.secure.Roles
import com.trevorism.secure.Secure
import com.trevorism.service.TestSuiteHealthService
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Inject

@Controller("/api/health")
class HealthController {

    @Inject
    TestSuiteHealthService testSuiteHealthService

    @Tag(name = "Health Operations")
    @Operation(summary = "Test suite health summary and detail **Secure")
    @Secure(Roles.USER)
    @Get(value = "/testsuite", produces = MediaType.APPLICATION_JSON)
    HealthPanel testSuiteHealth() {
        return testSuiteHealthService.getHealth()
    }
}
