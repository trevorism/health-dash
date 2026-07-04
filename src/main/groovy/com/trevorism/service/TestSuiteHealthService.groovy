package com.trevorism.service

import com.trevorism.data.FastDatastoreRepository
import com.trevorism.data.Repository
import com.trevorism.https.SecureHttpClient
import com.trevorism.model.HealthPanel
import com.trevorism.model.TestSuite

/**
 * Historical provider: reads the TestSuite datastore kind on demand and summarizes pass/fail.
 */
@jakarta.inject.Singleton
class TestSuiteHealthService extends PollingHealthProvider {

    private Repository<TestSuite> testSuiteRepository

    TestSuiteHealthService(SecureHttpClient httpClient) {
        testSuiteRepository = new FastDatastoreRepository<>(TestSuite, httpClient)
    }

    @Override
    String getKey() {
        return "testsuite"
    }

    @Override
    String getTitle() {
        return "Test Suites"
    }

    @Override
    protected HealthPanel load() {
        List<TestSuite> suites = testSuiteRepository.list()

        int total = suites.size()
        int passing = suites.count { it.lastRunSuccess } as int
        String status = total == 0 ? HealthPanel.STATUS_UNKNOWN : (passing == total ? HealthPanel.STATUS_OK : HealthPanel.STATUS_ERROR)

        return new HealthPanel(
                key: getKey(),
                title: getTitle(),
                status: status,
                headline: "${passing}/${total} suites passing"
        )
    }
}
