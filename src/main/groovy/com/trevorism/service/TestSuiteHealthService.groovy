package com.trevorism.service

import com.trevorism.data.FastDatastoreRepository
import com.trevorism.data.Repository
import com.trevorism.https.SecureHttpClient
import com.trevorism.model.HealthPanel
import com.trevorism.model.TestSuite

import org.slf4j.Logger
import org.slf4j.LoggerFactory

@jakarta.inject.Singleton
class TestSuiteHealthService {

    private static final Logger log = LoggerFactory.getLogger(TestSuiteHealthService)


    private Repository<TestSuite> testSuiteRepository

    TestSuiteHealthService(SecureHttpClient httpClient) {
        testSuiteRepository = new FastDatastoreRepository<>(TestSuite, httpClient)
    }

    HealthPanel getHealth() {
        try {
            List<TestSuite> suites = testSuiteRepository.list()

            int total = suites.size()
            int passing = suites.count { it.lastRunSuccess } as int
            String status = total == 0 ? HealthPanel.STATUS_UNKNOWN : (passing == total ? HealthPanel.STATUS_OK : HealthPanel.STATUS_ERROR)

            return new HealthPanel(
                    key: "testsuite",
                    title: "Test Suites",
                    status: status,
                    headline: "${passing}/${total} suites passing"
            )
        } catch (Exception e) {
            log.error("Unable to fetch test suite health", e)
            return new HealthPanel(
                    key: "testsuite",
                    title: "Test Suites",
                    status: HealthPanel.STATUS_UNKNOWN,
                    headline: "Unavailable"
            )
        }
    }
}
