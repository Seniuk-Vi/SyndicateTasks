package com.task05.model;

public class Event {
    private String id;
    private Long principalId;
    private String createdAt;
    private Content body;

    public Event() {
    }

    public Event(String id, Long principalId, String createdAt, Content body) {
        this.id = id;
        this.principalId = principalId;
        this.createdAt = createdAt;
        this.body = body;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getPrincipalId() {
        return principalId;
    }

    public void setPrincipalId(Long principalId) {
        this.principalId = principalId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Content getBody() {
        return body;
    }

    public void setBody(Content body) {
        this.body = body;
    }
}
