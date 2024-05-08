package hono.telemetry.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hono.telemetry.client.base.HonoClientApplicationBase;

public class App extends HonoClientApplicationBase {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(final String[] args) {
        
        logger.info("Hello, this is an info log.");
        logger.debug("This is a debug log.");
        logger.error("And here is an error log.");

        new App().consumeData();
    }
}
