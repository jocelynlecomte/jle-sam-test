package helloworld;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import software.amazon.awssdk.services.athena.AthenaClient;
import software.amazon.awssdk.services.athena.model.ListDatabasesRequest;
import software.amazon.awssdk.services.athena.model.ListDatabasesResponse;

/**
 * Handler for requests to Lambda function.
 */
public class AthenaApp implements RequestHandler<ScheduledEvent, Void> {
    @Override
    public Void handleRequest(ScheduledEvent input, Context context) {
        AthenaClient athenaClient = AthenaClient.builder().build();

        ListDatabasesRequest request = ListDatabasesRequest.builder().catalogName("AwsDataCatalog").build();
        ListDatabasesResponse response = athenaClient.listDatabases(request);

        response.databaseList().forEach(database -> System.out.println("Database: " + database.name()));

        return null;
    }
}
