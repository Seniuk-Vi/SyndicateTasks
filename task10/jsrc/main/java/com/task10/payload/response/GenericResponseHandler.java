package com.task10.payload.response;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

public class GenericResponseHandler {

    public static APIGatewayProxyResponseEvent successResponse(String message) {
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withBody(message);
    }

    public static APIGatewayProxyResponseEvent errorResponse(String errorMessage) {
        System.out.println("Error message: " + errorMessage);
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(400)
                .withBody(errorMessage);
    }

}
