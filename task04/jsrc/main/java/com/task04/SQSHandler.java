package com.task04;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.syndicate.deployment.annotations.events.SqsEvents;
import com.syndicate.deployment.annotations.events.SqsTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.events.EventSourceItem;

import java.util.HashMap;
import java.util.Map;

@LambdaHandler(lambdaName = "sqs_handler",
	roleName = "sqs_handler-role"
)
@SqsTriggerEventSource(targetQueue = "async_queue", batchSize = 1)
public class SQSHandler implements RequestHandler<SQSEvent, Void> {

	@Override
	public Void handleRequest(SQSEvent input, Context context) {
		System.out.println(input.getRecords().get(0).getBody());
		return null;
	}
}
