package telemetry.amqp.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        logger.info("Hello, this is an info log.");
        logger.debug("This is a debug log.");
        logger.error("And here is an error log.");
    }
}
