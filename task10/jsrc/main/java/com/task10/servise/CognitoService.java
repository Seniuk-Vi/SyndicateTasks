package com.task10.servise;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.task10.payload.response.GenericResponseHandler;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminConfirmSignUpRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminConfirmSignUpResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUserPoolClientsRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUserPoolClientsResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUserPoolsRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUserPoolsResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserPoolDescriptionType;

import java.util.HashMap;
import java.util.Map;

public class CognitoService {
    private CognitoIdentityProviderClient identityProviderClient;
    private final Region region = Region.EU_CENTRAL_1;

    public APIGatewayProxyResponseEvent signUp(APIGatewayProxyRequestEvent request) {
        JSONParser parser = new JSONParser();
        JSONObject bodyJson = null;
        try {
            bodyJson = (JSONObject) parser.parse(request.getBody());
        } catch (ParseException exc) {
            throw new RuntimeException(exc);
        }
        String firstName = (String) bodyJson.get("firstName");
        String lastName = (String) bodyJson.get("lastName");
        String email = (String) bodyJson.get("email");
        String password = (String) bodyJson.get("password");
        try {
            AdminConfirmSignUpResponse createUserResponse = registerUserInCognito(email, password, firstName, lastName);
            return createUserResponse.sdkHttpResponse().isSuccessful() ?
                    GenericResponseHandler.successResponse("User registered successfully") :
                    GenericResponseHandler.errorResponse("User registration failed");
        } catch (CognitoIdentityProviderException exc) {
            return GenericResponseHandler.errorResponse("User registration failed " + exc);
        }
    }
    public APIGatewayProxyResponseEvent signIn(APIGatewayProxyRequestEvent request) {
        JSONParser parser = new JSONParser();
        JSONObject bodyJson = null;
        try {
            bodyJson = (JSONObject) parser.parse(request.getBody());
        } catch (ParseException exc) {
            throw new RuntimeException(exc);
        }
        String email = (String) bodyJson.get("email");
        String password = (String) bodyJson.get("password");
        Map<String, String> authParameters = new HashMap<>();
        authParameters.put("USERNAME", email);
        authParameters.put("PASSWORD", password);
        try {
            AdminInitiateAuthRequest authRequest = AdminInitiateAuthRequest.builder()
                    .clientId(getClientId())
                    .userPoolId(getPoolId())
                    .authParameters(authParameters)
                    .authFlow(AuthFlowType.ADMIN_USER_PASSWORD_AUTH)
                    .build();

            AdminInitiateAuthResponse response = getCognitoIdentityProviderClient().adminInitiateAuth(authRequest);
            String accessToken = response.authenticationResult().idToken();
            JSONObject responseBody = new JSONObject();
            responseBody.put("accessToken", accessToken);

            return response.sdkHttpResponse().isSuccessful() ?
                    new APIGatewayProxyResponseEvent().withStatusCode(200).withBody(responseBody.toJSONString()) :
                    GenericResponseHandler.errorResponse("User signIn failed");
        } catch (CognitoIdentityProviderException exc) {
            return GenericResponseHandler.errorResponse("User signIn failed " + exc);
        }
    }
    private AdminConfirmSignUpResponse registerUserInCognito(String email, String password, String firstName, String lastName) {
        /*TODO In this case, you cannot use a single AttributeType instance to set multiple attributes.
          TODO Instead, you need to create a separate AttributeType for each attribute.*/
        AttributeType userAttrs = AttributeType.builder()
                .name("name").value(firstName + " " + lastName)
                .name("email").value(email)
                .build();
        SignUpRequest signUpRequest = SignUpRequest.builder()
                .userAttributes(userAttrs)
                .username(email)
                .clientId(getClientId())
                .password(password)
                .build();
        getCognitoIdentityProviderClient().signUp(signUpRequest);
        AdminConfirmSignUpRequest confirmSignUpRequest = AdminConfirmSignUpRequest.builder()
                .userPoolId(getPoolId())
                .username(email)
                .build();
        return getCognitoIdentityProviderClient().adminConfirmSignUp(confirmSignUpRequest);
    }
    private String getClientId() {
        ListUserPoolClientsRequest listUserPoolClientsRequest = ListUserPoolClientsRequest.builder()
                .userPoolId(getPoolId())
                .build();
        ListUserPoolClientsResponse listUserPoolClientsResponse = getCognitoIdentityProviderClient().listUserPoolClients(listUserPoolClientsRequest);
        return listUserPoolClientsResponse.userPoolClients().get(0).clientId();
    }

    private String getPoolId() {
        String userPoolName = "cmtr-6d93d07b-simple-booking-userpool-test";
        ListUserPoolsRequest listUserPoolsRequest = ListUserPoolsRequest.builder()
                .maxResults(10)
                .build();
        ListUserPoolsResponse listUserPoolsResponse = getCognitoIdentityProviderClient().listUserPools(listUserPoolsRequest);
        return listUserPoolsResponse.userPools().stream()
                .filter(pool -> userPoolName.equals(pool.name()))
                .findFirst()
                .map(UserPoolDescriptionType::id)
                .orElse(null);
    }
    private CognitoIdentityProviderClient getCognitoIdentityProviderClient() {
        if (identityProviderClient == null) {
            this.identityProviderClient = CognitoIdentityProviderClient.builder()
                    .region(region)
                    .build();
        }
        return identityProviderClient;
    }

}
