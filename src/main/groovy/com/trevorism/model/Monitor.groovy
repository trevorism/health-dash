package com.trevorism.model

/**
 * Local DTO mirroring the monitor service's Monitor datastore kind (the fields we surface).
 */
class Monitor {
    String id
    String source
    String kind
    String frequency
    Date startDate
    String testSuiteId
    String scheduleId
}
