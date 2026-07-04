package com.trevorism.service

import com.google.gson.Gson
import com.trevorism.http.HttpClient
import com.trevorism.http.JsonHttpClient
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Delegates refresh-token redemption to the central auth service (same flow as the login/memo
 * apps). We only need session renewal here, so this is scoped to redeemRefreshToken.
 */
@Singleton
class DefaultUserSessionService implements UserSessionService {

    private static final Logger log = LoggerFactory.getLogger(DefaultUserSessionService)

    private final HttpClient httpClient = new JsonHttpClient()
    private final Gson gson = new Gson()

    @Override
    String redeemRefreshToken(String refreshToken) {
        if (!refreshToken) {
            return null
        }
        try {
            String json = gson.toJson([refreshToken: refreshToken])
            String result = httpClient.post("https://auth.trevorism.com/token/refresh/redeem", json)
            if (result.startsWith("<html>")) {
                throw new RuntimeException("Bad Request to redeem refresh token")
            }
            return result
        } catch (Exception e) {
            log.debug("Unable to redeem refresh token", e)
        }
        return null
    }
}
