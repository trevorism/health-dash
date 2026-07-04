package com.trevorism.model

/**
 * Local DTO mirroring the fields returned by the testing service's GET /api/suite/.
 * Only the fields we need for the health glance are modeled.
 */
class TestSuite {
    String id
    String name
    String kind
    String source
    boolean lastRunSuccess
    Date lastRunDate
    int lastRuntimeSeconds
}
