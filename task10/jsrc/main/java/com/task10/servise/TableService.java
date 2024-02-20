package com.task10.servise;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task10.model.Table;
import com.task10.payload.response.ReservationsResponse;
import com.task10.payload.response.TablesResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

public class TableService {
    private final Regions REGION = Regions.EU_CENTRAL_1;
    /*TODO change TABLES_DB_TABLE_NAME*/
    private final String TABLES_DB_TABLE_NAME = "cmtr-6d93d07b-Tables-test";

    private AmazonDynamoDB amazonDynamoDB;

    private AmazonDynamoDB getAmazonDynamoDB() {
        if (amazonDynamoDB == null) {
            this.amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
                    .withRegion(REGION)
                    .build();
        }
        return amazonDynamoDB;
    }

    public TablesResponse getTables() {
        ScanRequest scanRequest = new ScanRequest().withTableName(TABLES_DB_TABLE_NAME);
        ScanResult result = getAmazonDynamoDB().scan(scanRequest);

        TablesResponse getTablesResponse = new TablesResponse();
        for (Map<String, AttributeValue> item : result.getItems()) {
            Table table = new Table();
            table.setId(Integer.parseInt(item.get("id").getN()));
            table.setNumber(Integer.parseInt(item.get("number").getN()));
            table.setPlaces(Integer.parseInt(item.get("places").getN()));
            table.setVip(Boolean.parseBoolean(String.valueOf(item.get("isVip").getBOOL())));
            if (item.containsKey("minOrder")) {
                table.setMinOrder(Integer.parseInt(item.get("minOrder").getN()));
            }
            getTablesResponse.getTables().add(table);
        }

        return getTablesResponse;
    }

    public Integer saveTable(String requestBody) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Table table = objectMapper.readValue(requestBody, Table.class);

        Table savedTable = new Table();
        savedTable.setId(table.getId());
        savedTable.setNumber(table.getNumber());
        savedTable.setVip(table.isVip());
        savedTable.setPlaces(table.getPlaces());
        savedTable.setMinOrder(table.getMinOrder());

        DynamoDBMapper dbMapper = new DynamoDBMapper(getAmazonDynamoDB());
        try {
            dbMapper.save(savedTable);
            return table.getId();
        } catch (Exception e) {
            return 1;
        }
    }


    public Table getTableById(int tableId) throws Exception {
        DynamoDB dynamoDB = new DynamoDB(getAmazonDynamoDB());
        com.amazonaws.services.dynamodbv2.document.Table dynamoTable = dynamoDB.getTable(TABLES_DB_TABLE_NAME);

        GetItemSpec getItemSpec = new GetItemSpec().withPrimaryKey("id", tableId);
        Item item = dynamoTable.getItem(getItemSpec);
        if (item == null) {
            throw new Exception("Table not found");
        }

        Table table = new Table();
        table.setId(item.getInt("id"));
        table.setNumber(item.getInt("number"));
        table.setPlaces(item.getInt("places"));
        table.setVip(item.getNumber("isVip").equals(BigDecimal.ONE));
        if (item.isPresent("minOrder")) {
            table.setMinOrder(item.getInt("minOrder"));
        }
        return table;
    }

}