package telemetry.amqp.consumer.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AmqpConsumerBase {
    protected static final Logger logger = LoggerFactory.getLogger(AmqpConsumerBase.class);

    /**
     * Start the consumer and set the message handling method to treat data that is received.
     */
    protected void consumeMessages() {
        logger.info("consumeMessages");
    }
}