package com.trevorism.model

/**
 * Local DTO mirroring the testing service's TestError datastore kind.
 */
class TestError {
    String id
    String source
    String message
    Date date
    Map details
}
