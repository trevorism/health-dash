package com.trevorism.model

/**
 * The common shape every health provider returns to the dashboard.
 * "summary" fields (status, headline) drive the at-a-glance tile; "details" carries
 * the underlying items for the drill-down view.
 */
class HealthPanel {
    public static final String STATUS_UNKNOWN = "UNKNOWN"
    public static final String STATUS_ERROR = "ERROR"
    public static final String STATUS_OK = "OK"
    public static final String STATUS_WARN = "WARNING"

    String id
    String key
    String title
    String status
    String headline
    Map details
}
