package com.task07;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.events.RuleEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.events.RuleEventSourceItem;
import com.task07.model.IdContainer;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@LambdaHandler(lambdaName = "uuid_generator",
	roleName = "task07-role"
)
@RuleEventSource(targetRule = "uuid_trigger")
public class Task07 implements RequestHandler<ScheduledEvent, Void> {
	private final AmazonS3 s3Client;
	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	private final Integer numberOfIds = 10;
	private final ObjectMapper objectMapper = new ObjectMapper();
	public Task07() {
		this.s3Client = AmazonS3ClientBuilder.standard()
				.withRegion("eu-central-1")
				.build();
	}
	@Override
	public Void handleRequest(ScheduledEvent input, Context context) {
		// create object
		List<String> ids = new ArrayList<>();
		for (int i = 0; i < numberOfIds; i++) {
			ids.add(java.util.UUID.randomUUID().toString());
		}
		IdContainer idContainer = IdContainer.builder()
				.ids(ids)
				.build();
		// filename with current time

		String fileName = getFileName();
		// put object into s3
		System.out.println("Putting object into s3: "+ idContainer);
		s3Client.putObject("cmtr-6d93d07b-uuid-storage-test", fileName, parseIdContainerToJson(idContainer));
		return null;
	}
	private String getFileName() {
		Instant instant = Instant.now();
		ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of("Z"));
		return formatter.format(zonedDateTime);
	}
	private String parseIdContainerToJson(IdContainer idContainer) {
        try {
            return objectMapper.writeValueAsString(idContainer) ;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
