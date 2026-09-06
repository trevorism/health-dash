Feature: Health dashboard API
  The dashboard aggregates platform health, so its endpoints must be alive and its
  data endpoints must reject unauthenticated callers

  Scenario: Ping is publicly available
    When I GET "api/ping" anonymously
    Then the response body is "pong"

  Scenario: Listing health panels requires authentication
    When I GET "api/health" anonymously
    Then the request is rejected

  Scenario: A single health panel requires authentication
    When I GET "api/health/testsuite" anonymously
    Then the request is rejected

  Scenario: The browser session is anonymous without cookies
    When I GET "api/auth/session" anonymously
    Then the response body reports nobody is signed in

  Scenario: Refreshing a session without a token is rejected
    When I POST "api/auth/refresh" as "application/x-www-form-urlencoded"
    Then the response status is 401

  Scenario: Logout accepts the content type a browser actually sends
    When I POST "api/auth/logout" as "application/x-www-form-urlencoded"
    Then the response status is 200

  Scenario: Signing in starts the one time code handoff
    When I GET "api/auth/login" without following redirects
    Then the response redirects to the login application with a callback on this host

  Scenario: The refresh endpoint the app used to carry is gone
    When I POST "api/refresh/" as "application/json"
    Then the response status is 404

  Scenario: An authenticated user sees the aggregated panels
    When I request all health panels as an authenticated user
    Then a health panel for "testsuite" is returned
    And every panel reports a status and a headline

  Scenario: An authenticated user can drill into a single panel
    When I request the "monitors" health panel as an authenticated user
    Then the returned panel has key "monitors"
