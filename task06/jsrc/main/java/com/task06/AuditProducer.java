package com.task06;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.syndicate.deployment.annotations.events.DynamoDbTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.task06.model.Configuration;
import com.task06.model.NewAudit;
import com.task06.model.UpdateAudit;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(lambdaName = "audit_producer",
        roleName = "task06-role"
)
@DynamoDbTriggerEventSource(targetTable = "Configuration", batchSize = 1)
public class AuditProducer implements RequestHandler<DynamodbEvent, Void> {
    private final AmazonDynamoDB dynamoDb;

    public AuditProducer() {
        this.dynamoDb = AmazonDynamoDBClient.builder()
                .withRegion("eu-central-1")
                .build();
    }

    @Override
    public Void handleRequest(DynamodbEvent event, Context context) {
        System.out.println("Hello from lambda");
        for (DynamodbEvent.DynamodbStreamRecord record : event.getRecords()) {
            if (record == null) {
                continue;
            }
            // check if the record is an INSERT or UPDATE
            Configuration configuration = parseConfiguration(record);
            if (record.getEventName().equals("INSERT")) {
                System.out.println("INSERT");
                NewAudit newAudit = NewAudit.builder()
                        .id(UUID.randomUUID().toString())
                        .itemKey(configuration.getKey())
                        .modificationTime(DateTimeFormatter.ISO_INSTANT.format(
                                Instant.ofEpochMilli(record.getDynamodb().getApproximateCreationDateTime().getTime())))
                        .newValue(configuration)
                        .build();
                publishAudit(newAudit);
            } else if (record.getEventName().equals("MODIFY")) {
                System.out.println("MODIFY");
                UpdateAudit updateAudit = UpdateAudit.builder()
                        .id(UUID.randomUUID().toString())
                        .itemKey(configuration.getKey())
                        .modificationTime(DateTimeFormatter.ISO_INSTANT.format(
                                Instant.ofEpochMilli(record.getDynamodb().getApproximateCreationDateTime().getTime())))
                        .updatedAttribute("value")
                        .oldValue(record.getDynamodb().getOldImage().get("value").getS())
                        .newValue(record.getDynamodb().getNewImage().get("value").getS())
                        .build();
                publishAudit(updateAudit);
            }
        }
        return null;
    }

    public Configuration parseConfiguration(DynamodbEvent.DynamodbStreamRecord record) {
        Configuration configuration = new Configuration();
        configuration.setKey(record.getDynamodb().getNewImage().get("key").getS());
        configuration.setValue(record.getDynamodb().getNewImage().get("value").getS());
        return configuration;
    }

    public void publishAudit(NewAudit newAudit) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("id", new AttributeValue().withS(newAudit.getId()));
        item.put("itemKey", new AttributeValue().withS(newAudit.getItemKey()));
        item.put("modificationTime", new AttributeValue().withS(newAudit.getModificationTime()));
        item.put("newValue", new AttributeValue().withM(new HashMap<String, AttributeValue>() {{
            put("key", new AttributeValue().withS(newAudit.getNewValue().getKey()));
            put("value", new AttributeValue().withS(newAudit.getNewValue().getValue()));
        }}));
        dynamoDb.putItem("cmtr-6d93d07b-Audit-test", item);
    }

    public void publishAudit(UpdateAudit audit) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("id", new AttributeValue().withS(audit.getId()));
        item.put("itemKey", new AttributeValue().withS(audit.getItemKey()));
        item.put("modificationTime", new AttributeValue().withS(audit.getModificationTime()));
        item.put("updatedAttribute", new AttributeValue().withS(audit.getUpdatedAttribute()));
        item.put("oldValue", new AttributeValue().withS(audit.getOldValue()));
        item.put("newValue", new AttributeValue().withS(audit.getNewValue()));
        dynamoDb.putItem("cmtr-6d93d07b-Audit-test", item);
    }
}
