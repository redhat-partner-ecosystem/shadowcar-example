package hono.telemetry.client.base;

public class HonoClientConstants {
    /**
     * The default host name to assume for interacting with Hono.
     */
    public static final String HONO_CONTAINER_HOST = "shadowcar-hono-dispatch-router-ext.shadowcar-hono.svc.cluster.local";
    /**
     * The name or IP address of the host to connect to for consuming messages.
     */
    public static final String HONO_MESSAGING_HOST = System.getProperty("consumer.host", HONO_CONTAINER_HOST);
    /**
     * Port of the AMQP network where consumers can receive data (in the standard setup this is the port
     * of the qdrouter).
     */
    public static final int HONO_AMQP_CONSUMER_PORT = Integer.parseInt(System.getProperty("consumer.port", "15671"));
    /**
     * Port of the Kafka bootstrap server where consumers can receive data.
     */
    public static final int HONO_KAFKA_CONSUMER_PORT = Integer.parseInt(System.getProperty("consumer.port", "9092"));

    public static final String TENANT_ID = "DEFAULT_TENANT";

    public static final String TRUSTSTORE_PATH = System.getProperty("truststore.path", "certs/truststore.pem");
    /**
     * For devices signaling that they remain connected for an indeterminate amount of time, a command is
     * periodically sent to the device after the following number of seconds elapsed.
     */
    public static final int COMMAND_INTERVAL_FOR_DEVICES_CONNECTED_WITH_UNLIMITED_EXPIRY =
            Integer.parseInt(System.getProperty("command.repetition.interval", "5"));

    private HonoClientConstants() {
        // prevent instantiation
    }
}
