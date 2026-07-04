package com.trevorism.service

import com.trevorism.data.FastDatastoreRepository
import com.trevorism.data.Repository
import com.trevorism.https.SecureHttpClient
import com.trevorism.model.HealthPanel
import com.trevorism.model.Question

/**
 * Historical provider: reads the Question datastore kind and reports outstanding prompts and
 * approvals across the platform. Overdue unanswered items are the anomaly (red); anything else
 * still pending is amber.
 */
@jakarta.inject.Singleton
class PromptHealthService extends PollingHealthProvider {

    private static final String APPROVAL = "approval"
    private static final int PENDING_LIMIT = 20

    private Repository<Question> questionRepository

    PromptHealthService(SecureHttpClient httpClient) {
        questionRepository = new FastDatastoreRepository<>(Question, httpClient)
    }

    @Override
    String getKey() {
        return "prompts"
    }

    @Override
    String getTitle() {
        return "Prompts & Approvals"
    }

    @Override
    protected HealthPanel load() {
        Date now = new Date()
        List<Question> pending = questionRepository.list().findAll { !it.answered }
        int approvals = pending.count { it.kind == APPROVAL } as int
        int overdue = pending.count { it.dueDate != null && it.dueDate.before(now) } as int

        String status
        if (overdue > 0) {
            status = HealthPanel.STATUS_ERROR
        } else if (!pending.isEmpty()) {
            status = HealthPanel.STATUS_WARN
        } else {
            status = HealthPanel.STATUS_OK
        }

        String headline = pending.isEmpty() ? "Nothing pending" :
                "${pending.size()} pending · ${approvals} approvals${overdue > 0 ? " · ${overdue} overdue" : ''}"

        return new HealthPanel(
                key: getKey(),
                title: getTitle(),
                status: status,
                headline: headline,
                details: [pending: pending.take(PENDING_LIMIT)]
        )
    }
}
