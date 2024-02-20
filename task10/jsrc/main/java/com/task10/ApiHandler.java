package com.task10;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.task10.model.Table;
import com.task10.payload.response.GenericResponseHandler;
import com.task10.payload.response.GetReservationsResponse;
import com.task10.payload.response.GetTablesResponse;
import com.task10.payload.response.ReservationsResponse;
import com.task10.payload.response.SaveReservationResponse;
import com.task10.payload.response.TablesResponse;
import com.task10.servise.CognitoService;
import com.task10.servise.ReservationService;
import com.task10.servise.TableService;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@LambdaHandler(lambdaName = "api_handler",
	roleName = "task02-role"
)
public class ApiHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
	private final TableService tableService = new TableService();
	private final ReservationService reservationService = new ReservationService();
	private final CognitoService cognitoClient = new CognitoService();
	private final Gson gson = new Gson();
	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
		String method = input.getHttpMethod();
		String resourcePath = input.getResource();

		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
		System.out.println("Request: " + input);
		switch (method) {
			case "POST":
				if ("/signup".equals(resourcePath)) {
					response = cognitoClient.signUp(input);
				} else if ("/signin".equals(resourcePath)) {
					response =cognitoClient.signIn(input);
				} else if ("/tables".equals(resourcePath)) {
					response = saveTable(input.getBody());
				} else if ("/reservations".equals(resourcePath)) {
					response = saveReservations(input);
				}
				break;
			case "GET":
				if ("/tables".equals(resourcePath)) {
					response = getTables();
				} else if (resourcePath.startsWith("/tables/")) {
					int tableId = Integer.parseInt(input.getPathParameters().get("tableId"));
					System.out.println("Table id: " + tableId);
					response = getTablesById(tableId);
				} else if ("/reservations".equals(resourcePath)) {
					response = getReservations();
				}
				break;
		}

		return response;
	}
	private APIGatewayProxyResponseEvent saveReservations(APIGatewayProxyRequestEvent request) {
		try {
			SaveReservationResponse response = reservationService.saveReservation(request.getBody());
			return GenericResponseHandler.successResponse(gson.toJson(response));
		} catch (IllegalArgumentException | IOException e) {
			return GenericResponseHandler.errorResponse(e.getMessage());
		}
	}
	private APIGatewayProxyResponseEvent saveTable(String request) {
		try {
			return GenericResponseHandler.successResponse(gson.toJson(tableService.saveTable(request)));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private APIGatewayProxyResponseEvent getTables() {
		GetTablesResponse response = tableService.getTables();
		return GenericResponseHandler.successResponse(gson.toJson(response));
	}
	private APIGatewayProxyResponseEvent getReservations() {
		GetReservationsResponse response = reservationService.getReservations();
		return GenericResponseHandler.successResponse(gson.toJson(response));
	}

	private APIGatewayProxyResponseEvent getTablesById(int id) {
		try {
			System.out.println("Get table by id...." + id);
			Table response = tableService.getTableById(id);
			System.out.println("Get table by id response: " + response);
			return GenericResponseHandler.successResponse(gson.toJson(response));
		} catch (Exception exp) {
			return GenericResponseHandler.errorResponse(exp.getMessage());
		}
	}
}