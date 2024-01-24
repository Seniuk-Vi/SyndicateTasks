package com.task04;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.syndicate.deployment.annotations.events.SnsEventSource;
import com.syndicate.deployment.annotations.events.SnsEvents;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.events.EventSourceItem;

import java.util.HashMap;
import java.util.Map;

@LambdaHandler(lambdaName = "sns_handler",
	roleName = "task04-role"
)
@SnsEventSource(targetTopic = "lambda_topic")
public class SNSHandler implements RequestHandler<SNSEvent, Void> {
	@Override
	public Void handleRequest(SNSEvent event, Context context) {
		System.out.println(event.getRecords().get(0).getSNS().getMessage());
		return null;
	}
}
