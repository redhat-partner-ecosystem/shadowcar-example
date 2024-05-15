package amqp.consumer.example.base;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.eclipse.hono.application.client.ApplicationClient;
import org.eclipse.hono.application.client.DownstreamMessage;
import org.eclipse.hono.application.client.MessageConsumer;
import org.eclipse.hono.application.client.MessageContext;
import org.eclipse.hono.application.client.TimeUntilDisconnectNotification;
import org.eclipse.hono.application.client.amqp.AmqpApplicationClient;
import org.eclipse.hono.application.client.amqp.ProtonBasedApplicationClient;
import org.eclipse.hono.client.ServiceInvocationException;
import org.eclipse.hono.client.amqp.config.ClientConfigProperties;
import org.eclipse.hono.client.amqp.connection.HonoConnection;
import org.eclipse.hono.util.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

public abstract class AmqpConsumerBase {
    // AMQP connectivity to the dispatch router
    public static final String CONSUMER_MESSAGING_HOST = System.getProperty("consumer.messaging.host",
            "shadowcar-hono-dispatch-router-ext.shadowcar-hono.svc.cluster.local");
    public static final int CONSUMER_MESSAGING_PORT = Integer
            .parseInt(System.getProperty("consumer.messaging.port", "15671"));

    // consumer credentials
    public static final String CONSUMER_USERNAME = System.getProperty("consumer.username", "consumer@HONO");
    public static final String CONSUMER_PASSWORD = System.getProperty("consumer.password", "verysecret");
    public static final Boolean PLAIN_CONNECTION = Boolean
            .valueOf(System.getProperty("consumer.plain.connection", "false"));
    public static final String TENANT_ID = System.getProperty("consumer.tenant", "DEFAULT_TENANT");
    public static final String TRUSTSTORE_PATH = System.getProperty("truststore.path", "certs/truststore.pem");

    // consumer behaviour
    public static final Boolean SEND_ONE_WAY_COMMANDS = Boolean
            .valueOf(System.getProperty("command.sendOneWayCommands", "false"));
    public static final int COMMAND_INTERVAL_FOR_DEVICES_CONNECTED_WITH_UNLIMITED_EXPIRY = Integer
            .parseInt(System.getProperty("command.repetition.interval", "5"));
    private static final String COMMAND_SEND_LIFECYCLE_INFO = "sendLifecycleInfo";

    /**
     * A map holding a handler to cancel a timer that was started to send commands
     * periodically to a device.
     * Only affects devices that use a connection oriented protocol like MQTT.
     */
    private final Map<String, Handler<Void>> periodicCommandSenderTimerCancelerMap = new HashMap<>();

    /**
     * A map holding the last reported notification for a device being connected.
     * Will be emptied as soon as the
     * notification is handled.
     * Only affects devices that use a connection oriented protocol like MQTT.
     */
    private final Map<String, TimeUntilDisconnectNotification> pendingTtdNotification = new HashMap<>();

    // other internal stuff
    private static final Random RAND = new Random();
    protected static final Logger logger = LoggerFactory.getLogger(AmqpConsumerBase.class);

    private MessageConsumer eventConsumer;
    private MessageConsumer telemetryConsumer;
    private final ApplicationClient<? extends MessageContext> client;
    private final Vertx vertx = Vertx.vertx();

    public AmqpConsumerBase() {
        client = createAmqpApplicationClient();
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
    protected abstract void handleTelemetryMessage(final DownstreamMessage<? extends MessageContext> msg);

    /**
     * Handler method for a Message from Hono that was received as event data.
     * <p>
     * The tenant, the device, the payload, the content-type, the creation-time and
     * the application properties will
     * be logged.
     *
     * @param msg The message that was received.
     */
    protected abstract void handleEventMessage(final DownstreamMessage<? extends MessageContext> msg);

    /**
     * Send a command to the device for which a
     * {@link TimeUntilDisconnectNotification} was received.
     * <p>
     * If the contained <em>ttd</em> is set to a value @gt; 0, the commandClient
     * will be closed after a response
     * was received.
     * If the contained <em>ttd</em> is set to -1, the commandClient will remain
     * open for further commands to be sent.
     * 
     * @param ttdNotification The ttd notification that was received for the device.
     */
    protected void sendCommandToAdapter(
            final String tenantId,
            final String deviceId,
            final TimeUntilDisconnectNotification ttdNotification) {

        final Duration commandTimeout = calculateCommandTimeout(ttdNotification);
        final Buffer commandBuffer = buildCommandPayload();
        final String command = "setBrightness";
        if (logger.isDebugEnabled()) {
            logger.debug("Sending command [{}] to [{}].", command, ttdNotification.getTenantAndDeviceId());
        }

        client.sendCommand(tenantId, deviceId, command, commandBuffer, "application/json", null, commandTimeout, null)
                .onSuccess(result -> {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Successfully sent command payload: [{}].", commandBuffer.toString());
                        logger.debug("And received response: [{}].", Optional.ofNullable(result.getPayload())
                                .orElseGet(Buffer::buffer).toString());
                    }
                })
                .onFailure(t -> {
                    if (t instanceof ServiceInvocationException) {
                        final int errorCode = ((ServiceInvocationException) t).getErrorCode();
                        logger.debug("Command was replied with error code [{}].", errorCode);
                    } else {
                        logger.debug("Could not send command : {}.", t.getMessage());
                    }
                });
    }

    /**
     * Send a one way command to the device for which a
     * {@link TimeUntilDisconnectNotification} was received.
     * <p>
     * If the contained <em>ttd</em> is set to a value @gt; 0, the commandClient
     * will be closed after a response
     * was received.
     * If the contained <em>ttd</em> is set to -1, the commandClient will remain
     * open for further commands to be sent.
     * 
     * @param ttdNotification The ttd notification that was received for the device.
     */
    private void sendOneWayCommandToAdapter(final String tenantId, final String deviceId,
            final TimeUntilDisconnectNotification ttdNotification) {

        final Buffer commandBuffer = buildOneWayCommandPayload();

        if (logger.isDebugEnabled()) {
            logger.debug("Sending one-way command [{}] to [{}].",
                    COMMAND_SEND_LIFECYCLE_INFO, ttdNotification.getTenantAndDeviceId());
        }

        client.sendOneWayCommand(tenantId, deviceId, COMMAND_SEND_LIFECYCLE_INFO, commandBuffer)
                .onSuccess(statusResult -> {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Successfully sent one-way command payload: [{}] and received status [{}].",
                                commandBuffer.toString(), statusResult);
                    }
                })
                .onFailure(t -> {
                    if (t instanceof ServiceInvocationException) {
                        final int errorCode = ((ServiceInvocationException) t).getErrorCode();
                        logger.debug("One-way command was replied with error code [{}].", errorCode);
                    } else {
                        logger.debug("Could not send one-way command : {}.", t.getMessage());
                    }
                });
    }

    /**
     * Sends a command to the device for which a
     * {@link TimeUntilDisconnectNotification} was received.
     *
     * @param notification The notification that was received for the device.
     */

    protected void sendCommand(final TimeUntilDisconnectNotification notification) {

        if (SEND_ONE_WAY_COMMANDS) {
            sendOneWayCommandToAdapter(notification.getTenantId(), notification.getDeviceId(), notification);
        } else {
            sendCommandToAdapter(notification.getTenantId(), notification.getDeviceId(), notification);
        }
    }

    /**
     * Start the consumer and set the message handling method to treat data that is
     * received.
     */
    protected synchronized void consumeMessages() {
        final CompletableFuture<ApplicationClient<? extends MessageContext>> startup = new CompletableFuture<>();

        final AmqpApplicationClient ac = (AmqpApplicationClient) client;
        ac.addDisconnectListener(c -> logger.info("lost connection to Hono, trying to reconnect ..."));
        ac.addReconnectListener(c -> logger.info("reconnected to Hono"));

        final Promise<Void> readyTracker = Promise.promise();
        client.addOnClientReadyHandler(readyTracker);
        client.start()
                .compose(ok -> readyTracker.future())
                .compose(v -> Future.all(createEventConsumer(), createTelemetryConsumer()))
                .onSuccess(ok -> startup.complete(client))
                .onFailure(startup::completeExceptionally);

        try {
            startup.join();
            logger.info("Consumer ready for telemetry and event messages. Tenant={}", TENANT_ID);
            
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); 
                System.err.println("Thread Interrupted");
            }

        } catch (final CompletionException e) {
            logger.error("{} consumer failed to start [{}:{}]", "AMQP", CONSUMER_MESSAGING_HOST,
                    CONSUMER_MESSAGING_PORT, e.getCause());
        }

        final CompletableFuture<ApplicationClient<? extends MessageContext>> shutDown = new CompletableFuture<>();

        final List<Future<Void>> closeFutures = new ArrayList<>();
        Optional.ofNullable(eventConsumer)
                .map(MessageConsumer::close)
                .ifPresent(closeFutures::add);
        Optional.ofNullable(telemetryConsumer)
                .map(MessageConsumer::close)
                .ifPresent(closeFutures::add);
        Optional.of(client)
                .map(Lifecycle::stop)
                .ifPresent(closeFutures::add);

        Future.join(closeFutures)
                .compose(ok -> vertx.close())
                .recover(t -> vertx.close())
                .onComplete(ar -> shutDown.complete(client));

        // wait for clients to be closed
        shutDown.join();
        logger.info("Consumer has been shut down");
    }

    /**
     * Create the message consumer that handles event messages and invokes the
     * notification callback
     * {@link #handleCommandReadinessNotification(TimeUntilDisconnectNotification)}
     * if the message indicates that it
     * stays connected for a specified time.
     *
     * @return A succeeded future if the creation was successful, a failed Future
     *         otherwise.
     */
    protected Future<MessageConsumer> createEventConsumer() {
        return client.createEventConsumer(
                TENANT_ID,
                msg -> {
                    // handle command readiness notification
                    msg.getTimeUntilDisconnectNotification().ifPresent(this::handleCommandReadinessNotification);
                    handleEventMessage(msg);
                },
                cause -> logger.error("event consumer closed by remote", cause))
                .onSuccess(consumer -> this.eventConsumer = consumer);
    }

    /**
     * Create the message consumer that handles telemetry messages and invokes the
     * notification callback
     * {@link #handleCommandReadinessNotification(TimeUntilDisconnectNotification)}
     * if the message indicates that it
     * stays connected for a specified time.
     *
     * @return A succeeded future if the creation was successful, a failed Future
     *         otherwise.
     */
    private Future<MessageConsumer> createTelemetryConsumer() {
        return client.createTelemetryConsumer(
                TENANT_ID,
                msg -> {
                    // handle command readiness notification
                    msg.getTimeUntilDisconnectNotification().ifPresent(this::handleCommandReadinessNotification);
                    handleTelemetryMessage(msg);
                },
                cause -> logger.error("telemetry consumer closed by remote", cause))
                .onSuccess(consumer -> this.telemetryConsumer = consumer);
    }

    /**
     * Handler method for a <em>device ready for command</em> notification (by an
     * explicit event or contained
     * implicitly in another message).
     * <p>
     * For notifications with a positive ttd value (as usual for request-response
     * protocols), the
     * code creates a simple command in JSON.
     * <p>
     * For notifications signaling a connection oriented protocol, the handling is
     * delegated to
     * {@link #handlePermanentlyConnectedCommandReadinessNotification(TimeUntilDisconnectNotification)}.
     *
     * @param notification The notification containing the tenantId, deviceId and
     *                     the Instant (that
     *                     defines until when this notification is valid). See
     *                     {@link TimeUntilDisconnectNotification}.
     */
    private void handleCommandReadinessNotification(final TimeUntilDisconnectNotification notification) {
        if (notification.getTtd() <= 0) {
            handlePermanentlyConnectedCommandReadinessNotification(notification);
        } else {
            logger.info("Device is ready to receive a command : [{}].", notification);
            sendCommand(notification);
        }
    }

    /**
     * Handle a ttd notification for permanently connected devices.
     * <p>
     * Instead of immediately handling the notification, it is first put to a map
     * and a timer is started to handle it
     * later. Notifications for the same device that are received before the timer
     * expired, will overwrite the original
     * notification. By this an <em>event flickering</em> (like it could occur when
     * starting the app while several
     * notifications were persisted in the messaging network) is handled correctly.
     * <p>
     * If the contained <em>ttd</em> is set to -1, a command will be sent
     * periodically every
     * {@link COMMAND_INTERVAL_FOR_DEVICES_CONNECTED_WITH_UNLIMITED_EXPIRY} seconds
     * to the device
     * until a new notification was received with a <em>ttd</em> set to 0.
     *
     * @param notification The notification of a permanently connected device to
     *                     handle.
     */
    private void handlePermanentlyConnectedCommandReadinessNotification(
            final TimeUntilDisconnectNotification notification) {

        final String keyForDevice = notification.getTenantAndDeviceId();

        final TimeUntilDisconnectNotification previousNotification = pendingTtdNotification.get(keyForDevice);
        if (previousNotification != null) {
            if (notification.getCreationTime().isAfter(previousNotification.getCreationTime())) {
                logger.info("Set new ttd value [{}] of notification for [{}]",
                        notification.getTtd(), notification.getTenantAndDeviceId());
                pendingTtdNotification.put(keyForDevice, notification);
            } else {
                logger.trace("Received notification for [{}] that was already superseded by newer [{}]",
                        notification, previousNotification);
            }
        } else {
            pendingTtdNotification.put(keyForDevice, notification);
            // there was no notification available already, so start a handler now
            vertx.setTimer(1000, timerId -> {
                logger.debug("Handle device notification for [{}].", notification.getTenantAndDeviceId());
                // now take the notification from the pending map and handle it
                final TimeUntilDisconnectNotification notificationToHandle = pendingTtdNotification
                        .remove(keyForDevice);
                if (notificationToHandle != null) {
                    if (notificationToHandle.getTtd() == -1) {
                        logger.info("Device notified as being ready to receive a command until further notice : [{}].",
                                notificationToHandle);

                        // cancel a still existing timer for this device (if found)
                        cancelPeriodicCommandSender(notification);
                        // immediately send the first command
                        sendCommand(notificationToHandle);

                        // for devices that stay connected, start a periodic timer now that repeatedly
                        // sends a command
                        // to the device
                        vertx.setPeriodic(
                                (long) COMMAND_INTERVAL_FOR_DEVICES_CONNECTED_WITH_UNLIMITED_EXPIRY
                                        * 1000,
                                id -> {
                                    sendCommand(notificationToHandle);
                                    // register a canceler for this timer directly after it was created
                                    setPeriodicCommandSenderTimerCanceler(id, notification);
                                });
                    } else {
                        logger.info("Device notified as not being ready to receive a command (anymore) : [{}].",
                                notification);
                        cancelPeriodicCommandSender(notificationToHandle);
                        logger.debug("Device will not receive further commands : [{}].",
                                notification.getTenantAndDeviceId());
                    }
                }
            });
        }
    }

    /**
     * Calculate the timeout for a command that is tried to be sent to a device for
     * which a
     * {@link TimeUntilDisconnectNotification} was received.
     *
     * @param notification The notification that was received for the device.
     * @return The timeout (milliseconds) to be set for the command.
     */
    private Duration calculateCommandTimeout(final TimeUntilDisconnectNotification notification) {

        if (notification.getTtd() == -1) {
            // let the command expire directly before the next periodic timer is started
            return Duration.ofMillis(COMMAND_INTERVAL_FOR_DEVICES_CONNECTED_WITH_UNLIMITED_EXPIRY * 1000L);
        } else {
            // let the command expire when the notification expires
            return Duration.ofMillis(notification.getMillisecondsUntilExpiry());
        }
    }

    private void setPeriodicCommandSenderTimerCanceler(final Long timerId,
            final TimeUntilDisconnectNotification ttdNotification) {
        this.periodicCommandSenderTimerCancelerMap.put(ttdNotification.getTenantAndDeviceId(), v -> {
            vertx.cancelTimer(timerId);
            periodicCommandSenderTimerCancelerMap.remove(ttdNotification.getTenantAndDeviceId());
        });
    }

    private void cancelPeriodicCommandSender(final TimeUntilDisconnectNotification notification) {
        if (isPeriodicCommandSenderActiveForDevice(notification)) {
            logger.debug("Cancelling periodic sender for {}", notification.getTenantAndDeviceId());
            periodicCommandSenderTimerCancelerMap.get(notification.getTenantAndDeviceId()).handle(null);
        } else {
            logger.debug("Wanted to cancel periodic sender for {}, but could not find one",
                    notification.getTenantAndDeviceId());
        }
    }

    private boolean isPeriodicCommandSenderActiveForDevice(final TimeUntilDisconnectNotification notification) {
        return periodicCommandSenderTimerCancelerMap.containsKey(notification.getTenantAndDeviceId());
    }

    private static Buffer buildCommandPayload() {
        final JsonObject jsonCmd = new JsonObject().put("brightness", RAND.nextInt(100));
        return Buffer.buffer(jsonCmd.encodePrettily());
    }

    private static Buffer buildOneWayCommandPayload() {
        final JsonObject jsonCmd = new JsonObject().put("info", "app restarted.");
        return Buffer.buffer(jsonCmd.encodePrettily());
    }

    /**
     * The consumer needs one connection to the AMQP 1.0 messaging network from
     * which it can consume data.
     * <p>
     * The client for receiving data is instantiated here.
     */
    private ApplicationClient<? extends MessageContext> createAmqpApplicationClient() {

        // FIXME move all of this to the constructor !
        
        final ClientConfigProperties props = new ClientConfigProperties();
        props.setLinkEstablishmentTimeout(5000L);
        props.setHost(CONSUMER_MESSAGING_HOST);
        props.setPort(CONSUMER_MESSAGING_PORT);

        if (!PLAIN_CONNECTION) {
            props.setUsername(CONSUMER_USERNAME);
            props.setPassword(CONSUMER_PASSWORD);
            props.setTlsEnabled(true);
            props.setServerRole("AMQP Messaging Network");
            props.setTrustStorePath(TRUSTSTORE_PATH);
            props.setHostnameVerificationRequired(false);
        }

        return new ProtonBasedApplicationClient(HonoConnection.newConnection(vertx, props));
    }
}