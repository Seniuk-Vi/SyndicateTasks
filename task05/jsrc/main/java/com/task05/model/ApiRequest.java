package com.task05.model;

public class ApiRequest {
    private Long principalId;
    private Content content;

    public ApiRequest() {
    }

    public ApiRequest(Long principalId, Content content) {
        this.principalId = principalId;
        this.content = content;
    }

    public Long getPrincipalId() {
        return principalId;
    }

    public void setPrincipalId(Long principalId) {
        this.principalId = principalId;
    }

    public Content getContent() {
        return content;
    }

    public void setContent(Content content) {
        this.content = content;
    }
}
