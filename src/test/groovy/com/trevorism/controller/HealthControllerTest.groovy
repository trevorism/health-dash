package com.trevorism.controller

import com.trevorism.model.HealthPanel
import com.trevorism.secure.Roles
import com.trevorism.service.HealthProvider
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.security.authentication.Authentication
import org.junit.jupiter.api.Test

class HealthControllerTest {

    private static HealthProvider provider(String key, boolean adminOnly = false) {
        return [
                getKey   : { -> key },
                getTitle : { -> key.capitalize() },
                getHealth: { -> new HealthPanel(key: key, status: HealthPanel.STATUS_OK) },
                adminOnly: { -> adminOnly }
        ] as HealthProvider
    }

    private static Authentication auth(List<String> roles) {
        return [getRoles: { -> roles }, getName: { -> "tester" }, getAttributes: { -> [:] }] as Authentication
    }

    @Test
    void testAllHealthHidesAdminOnlyPanelsFromNonAdmin() {
        HealthController controller = new HealthController([provider("public"), provider("secret", true)])

        List<HealthPanel> panels = controller.allHealth(auth([Roles.USER]))

        assert panels.size() == 1
        assert panels[0].key == "public"
    }

    @Test
    void testAllHealthShowsAdminOnlyPanelsToAdmin() {
        HealthController controller = new HealthController([provider("public"), provider("secret", true)])

        List<HealthPanel> panels = controller.allHealth(auth([Roles.USER, Roles.ADMIN]))

        assert panels.collect { it.key }.toSet() == ["public", "secret"].toSet()
    }

    @Test
    void testHealthReturnsPanelForKnownKey() {
        HealthController controller = new HealthController([provider("public")])

        HttpResponse<HealthPanel> response = controller.health("public", auth([Roles.USER]))

        assert response.status() == HttpStatus.OK
        assert response.body().key == "public"
    }

    @Test
    void testHealthReturnsNotFoundForUnknownKey() {
        HealthController controller = new HealthController([provider("public")])

        HttpResponse<HealthPanel> response = controller.health("missing", auth([Roles.USER]))

        assert response.status() == HttpStatus.NOT_FOUND
    }

    @Test
    void testHealthReturnsNotFoundForAdminOnlyKeyWhenNonAdmin() {
        HealthController controller = new HealthController([provider("secret", true)])

        HttpResponse<HealthPanel> response = controller.health("secret", auth([Roles.USER]))

        assert response.status() == HttpStatus.NOT_FOUND
    }

    @Test
    void testHealthReturnsAdminOnlyKeyForAdmin() {
        HealthController controller = new HealthController([provider("secret", true)])

        HttpResponse<HealthPanel> response = controller.health("secret", auth([Roles.USER, Roles.ADMIN]))

        assert response.status() == HttpStatus.OK
        assert response.body().key == "secret"
    }
}
