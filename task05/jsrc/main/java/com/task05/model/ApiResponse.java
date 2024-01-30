package com.task05.model;

public class ApiResponse {
    private Integer StatusCode;
    private Event event;

    public ApiResponse() {
    }

    public ApiResponse(Integer statusCode, Event event) {
        StatusCode = statusCode;
        this.event = event;
    }

    public Integer getStatusCode() {
        return StatusCode;
    }

    public void setStatusCode(Integer statusCode) {
        StatusCode = statusCode;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }
}
