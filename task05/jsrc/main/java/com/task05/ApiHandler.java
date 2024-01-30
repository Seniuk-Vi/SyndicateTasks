package com.task05;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.LambdaUrlConfig;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;
import com.task05.model.ApiRequest;
import com.task05.model.ApiResponse;
import com.task05.model.Event;
import org.apache.http.HttpStatus;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(lambdaName = "api_handler",
        roleName = "task05-role"
)
@LambdaUrlConfig(
        authType = AuthType.NONE,
        invokeMode = InvokeMode.BUFFERED
)
public class ApiHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final AmazonDynamoDB dynamoDB;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ApiHandler() {
        dynamoDB = AmazonDynamoDBClient.builder()
                .withRegion("eu-central-1")
                .build();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        System.out.println("Received event: " + event);
        if ("/events".contains(event.getPath()) && "POST".equals(event.getHttpMethod())) {
            System.out.println("Received POST request: "+ event.getBody());
            // Process the request
            ApiRequest request = parseRequest(event.getBody());
            ApiResponse response = generateApiResponse(request);
            // save to dynamo db
            PutItemRequest putItemRequest = new PutItemRequest("cmtr-6d93d07b-Events-test", toDynamoDBItem(response));
            dynamoDB.putItem(putItemRequest);
            // return response
            APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent();
            responseEvent.setStatusCode(HttpStatus.SC_CREATED);
            // set event = PutItemRequest
            responseEvent.setBody(parseResponse(response));
            return responseEvent;
        }
        System.out.println("Unsupported  request");
        return null; // or handle other paths and HTTP methods as necessary
    }

    public ApiRequest parseRequest(String event) {
        try {
            return objectMapper.readValue(event, ApiRequest.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String parseResponse(ApiResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String parseContent(Map<String, String> content) {
        try {
            return objectMapper.writeValueAsString(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ApiResponse generateApiResponse(ApiRequest request) {
        return new ApiResponse(201,
                new Event(UUID.randomUUID().toString(),
                        request.getPrincipalId(),
                        Instant.now().toString(),
                        request.getContent()));
    }

    public Map<String, AttributeValue> toDynamoDBItem(ApiResponse response) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("id", new AttributeValue(response.getEvent().getId()));
        item.put("principalId", new AttributeValue().withN(response.getEvent().getPrincipalId().toString()));
        item.put("createdAt", new AttributeValue().withS(response.getEvent().getCreatedAt()));
        item.put("body", new AttributeValue().withS(parseContent(response.getEvent().getBody())));
        return item;
    }
}
