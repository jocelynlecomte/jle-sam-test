package helloworld;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;

public class SimpleApp implements RequestHandler<ScheduledEvent, Void> {
    @Override
    public Void handleRequest(ScheduledEvent input, Context context) {
        System.out.println("Hey, isn't it really " + input.getDetail().get("word") + " !!!");

        return null;
    }
}
