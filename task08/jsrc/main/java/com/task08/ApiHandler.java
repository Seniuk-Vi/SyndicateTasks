package com.task08;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.syndicate.deployment.annotations.LambdaUrlConfig;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaLayer;
import com.syndicate.deployment.model.ArtifactExtension;
import com.syndicate.deployment.model.DeploymentRuntime;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;
import com.task08.service.OpenMeteoService;

import java.io.IOException;

@LambdaHandler(
		lambdaName = "api_handler",
		roleName = "api_handler-role",
		layers = { "sdk-layer" }
)
@LambdaLayer(
		layerName = "sdk-layer",
		libraries = { "lib/commons-lang3-3.14.0.jar" },
		runtime = DeploymentRuntime.JAVA8,
		artifactExtension = ArtifactExtension.ZIP
)
@LambdaUrlConfig(
		authType = AuthType.NONE,
		invokeMode = InvokeMode.BUFFERED
)
public class ApiHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
	private static final int SC_OK = 200;
	private static final int SC_SERVER_ERROR = 500;
	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context)  {
		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
		try {
			double latitude = 52.52;
			double longitude = 13.41;

			String weatherData = OpenMeteoService.getLatestWeatherForecast(latitude, longitude, "temperature_2m");

			return response
					.withStatusCode(SC_OK)
					.withBody(weatherData);
		}
		catch (IOException | NumberFormatException e) {
			return response
					.withStatusCode(SC_SERVER_ERROR)
					.withBody("Internal server error: " + e.getMessage());
		}
    }
}
