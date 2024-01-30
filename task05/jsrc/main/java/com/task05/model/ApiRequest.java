package com.task05.model;

import java.util.Map;

public class ApiRequest {
    private Long principalId;
    private Map<String, String> content;

    public ApiRequest() {
    }

    public ApiRequest(Long principalId, Map<String, String> content) {
        this.principalId = principalId;
        this.content = content;
    }

    public Long getPrincipalId() {
        return principalId;
    }

    public void setPrincipalId(Long principalId) {
        this.principalId = principalId;
    }

    public Map<String, String> getContent() {
        return content;
    }

    public void setContent(Map<String, String> content) {
        this.content = content;
    }
}
