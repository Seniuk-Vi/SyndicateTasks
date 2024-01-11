package com.task02;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.syndicate.deployment.annotations.LambdaUrlConfig;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;

@LambdaHandler(
        lambdaName = "hello_world",
        roleName = "task02-role",
        isPublishVersion = true,
        aliasName = "${lambdas_alias_name}"
)
@LambdaUrlConfig(
        authType = AuthType.AWS_IAM,
        invokeMode = InvokeMode.BUFFERED
)
public class HelloWorld implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        System.out.printf("Event: %s\n", event);
        if ("/hello".equals(event.getPath()) && "GET".equals(event.getHttpMethod())) {
            // Process the GET request
            APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
            response.setStatusCode(200);
            response.setBody("{\n" +
                    "    \"statusCode\": 200,\n" +
                    "    \"message\": \"Hello from Lambda\"\n" +
                    "}");
            return response;
        }
        return null; // or handle other paths and HTTP methods as necessary
    }
}