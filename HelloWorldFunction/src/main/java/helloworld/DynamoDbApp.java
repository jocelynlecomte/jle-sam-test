package helloworld;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DynamoDbApp implements RequestHandler<ScheduledEvent, Integer> {
    public static final String FEELINGS_TABLE = "feelings_table";

    @Override
    public Integer handleRequest(ScheduledEvent input, Context context) {
        DynamoDbClient ddbClient = DynamoDbClient.builder()
                .endpointOverride(URI.create("http://dynamodb-local:8000"))
                .build();

        storeFeeling(ddbClient, input.getDetail().get("word").toString());

        return getFeelingsCount(ddbClient);
    }

    public void storeFeeling(DynamoDbClient client, String feeling) {
        Map<String, AttributeValue> itemValues = new HashMap<>();
        itemValues.put("feelingId", AttributeValue.builder().s(UUID.randomUUID().toString()).build());
        itemValues.put("feeling", AttributeValue.builder().s(feeling).build());

        PutItemRequest request = PutItemRequest.builder()
                .tableName(FEELINGS_TABLE)
                .item(itemValues)
                .build();

        try {
            client.putItem(request);
            System.out.println("Feelings were successfully updated");
        } catch (ResourceNotFoundException e) {
            System.err.format("Error: The Amazon DynamoDB table \"%s\" can't be found.\n", FEELINGS_TABLE);
            System.err.println("Be sure that it exists and that you've typed its name correctly!");
        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
        }
    }

    public int getFeelingsCount(DynamoDbClient client) {
        ScanRequest request = ScanRequest.builder().tableName(FEELINGS_TABLE).build();
        try {
            ScanResponse response = client.scan(request);
            System.out.println("Feelings were successfully scanned");
            return response.count();

        } catch (ResourceNotFoundException e) {
            System.err.format("Error: The Amazon DynamoDB table \"%s\" can't be found.\n", FEELINGS_TABLE);
            System.err.println("Be sure that it exists and that you've typed its name correctly!");
        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
        }
        return -1;
    }
}
