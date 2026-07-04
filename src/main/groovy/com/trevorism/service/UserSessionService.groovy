package com.trevorism.service

interface UserSessionService {

    /** Exchange a refresh token for a fresh session (access) token, or null if it can't be redeemed. */
    String redeemRefreshToken(String refreshToken)
}
