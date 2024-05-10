package telemetry.amqp.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import telemetry.amqp.consumer.base.AmqpConsumerBase;

public class App extends AmqpConsumerBase {
    //private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        new App().consumeMessages();
    }
}
