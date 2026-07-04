package com.trevorism.service

import com.trevorism.data.Repository
import com.trevorism.model.HealthPanel
import com.trevorism.model.Question
import org.junit.jupiter.api.Test

class PromptHealthServiceTest {

    private static PromptHealthService serviceReturning(List<Question> questions) {
        PromptHealthService service = new PromptHealthService(null)
        service.@questionRepository = [list: { -> questions }] as Repository
        return service
    }

    @Test
    void testOkWhenNothingPending() {
        List<Question> qs = [new Question(answered: true), new Question(answered: true)]
        HealthPanel panel = serviceReturning(qs).load()
        assert panel.status == HealthPanel.STATUS_OK
        assert panel.headline == "Nothing pending"
    }

    @Test
    void testWarnWhenPendingButNoneOverdue() {
        List<Question> qs = [
                new Question(answered: false, kind: "approval"),
                new Question(answered: false, kind: "question"),
                new Question(answered: true)
        ]
        HealthPanel panel = serviceReturning(qs).load()
        assert panel.status == HealthPanel.STATUS_WARN
        assert panel.headline == "2 pending · 1 approvals"
    }

    @Test
    void testErrorWhenOverduePending() {
        Date past = new Date(System.currentTimeMillis() - 100_000L)
        List<Question> qs = [
                new Question(answered: false, kind: "approval", dueDate: past),
                new Question(answered: false, kind: "question")
        ]
        HealthPanel panel = serviceReturning(qs).load()
        assert panel.status == HealthPanel.STATUS_ERROR
        assert panel.headline == "2 pending · 1 approvals · 1 overdue"
    }

    @Test
    void testFutureDueDateIsNotOverdue() {
        Date future = new Date(System.currentTimeMillis() + 100_000L)
        List<Question> qs = [new Question(answered: false, kind: "question", dueDate: future)]
        HealthPanel panel = serviceReturning(qs).load()
        assert panel.status == HealthPanel.STATUS_WARN
        assert panel.headline == "1 pending · 0 approvals"
    }

    @Test
    void testPendingDetailsCappedAtLimit() {
        List<Question> qs = (1..25).collect { new Question(answered: false, kind: "question") }
        HealthPanel panel = serviceReturning(qs).load()
        assert panel.details.pending.size() == 20
    }
}
