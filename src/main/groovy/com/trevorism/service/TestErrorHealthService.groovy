package com.trevorism.service

import com.trevorism.data.FastDatastoreRepository
import com.trevorism.data.Repository
import com.trevorism.data.model.sorting.Sort
import com.trevorism.https.SecureHttpClient
import com.trevorism.model.HealthPanel
import com.trevorism.model.TestError

/**
 * Historical provider: reads the TestError datastore kind, newest first. Errors are unresolved
 * anomalies (they're scrubbed after 7 days / removed on resolution), so any error is a red state.
 */
@jakarta.inject.Singleton
class TestErrorHealthService extends PollingHealthProvider {

    private static final int RECENT_LIMIT = 20

    private Repository<TestError> errorRepository

    TestErrorHealthService(SecureHttpClient httpClient) {
        errorRepository = new FastDatastoreRepository<>(TestError, httpClient)
    }

    @Override
    String getKey() {
        return "testerrors"
    }

    @Override
    String getTitle() {
        return "Test Errors"
    }

    @Override
    protected HealthPanel load() {
        List<TestError> errors = errorRepository.sort(new Sort("date", true))

        int total = errors.size()
        String headline = total == 0 ? "No active errors" : "${total} unresolved · latest: ${errors[0].source}"

        return new HealthPanel(
                key: getKey(),
                title: getTitle(),
                status: total == 0 ? HealthPanel.STATUS_OK : HealthPanel.STATUS_ERROR,
                headline: headline,
                details: [errors: errors.take(RECENT_LIMIT)]
        )
    }
}
