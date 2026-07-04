package com.trevorism.model

/**
 * Local DTO mirroring the prompt service's Question datastore kind (fields we surface).
 * kind is "question" or "approval".
 */
class Question {
    String id
    String text
    Date createDate
    boolean answered
    Date dueDate
    String kind
}
