package amqp.consumer.example;

import org.eclipse.hono.application.client.DownstreamMessage;
import org.eclipse.hono.application.client.MessageContext;

import amqp.consumer.example.base.AmqpConsumerBase;

public class AmqpConsumerApp extends AmqpConsumerBase {
    
    public static void main(String[] args) {
        new AmqpConsumerApp().consumeMessages();
    }

    /**
     * Handler method for a Message from Hono that was received as telemetry data.
     * <p>
     * The tenant, the device, the payload, the content-type, the creation-time and
     * the application properties
     * will be logged.
     *
     * @param msg The message that was received.
     */
    protected void handleTelemetryMessage(final DownstreamMessage<? extends MessageContext> msg) {
        logger.info("received telemetry data [tenant: {}, device: {}, content-type: {}]: [{}].",
                msg.getTenantId(), msg.getDeviceId(), msg.getContentType(), msg.getPayload());
    }

    /**
     * Handler method for a Message from Hono that was received as event data.
     * <p>
     * The tenant, the device, the payload, the content-type, the creation-time and
     * the application properties will
     * be logged.
     *
     * @param msg The message that was received.
     */
    protected void handleEventMessage(final DownstreamMessage<? extends MessageContext> msg) {
        logger.info("received event [tenant: {}, device: {}, content-type: {}]: [{}].",
                msg.getTenantId(), msg.getDeviceId(), msg.getContentType(), msg.getPayload());
    }

    
}
