package mqtt.producer.example.base;

import java.util.UUID;

import javax.net.ssl.SSLSocketFactory;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public abstract class MqttProducerBase {

    // connectivity to the MQTt adapter
    public static final String MQTT_HOST = System.getProperty("mqtt.host", "shadowcar-hono-adapter-mqtt.shadowcar-hono.svc.cluster.local");
    public static final int MQTT_PORT = Integer.parseInt(System.getProperty("mqtt.port", "8883"));
    public static final String MQTT_PROTOCOL = System.getProperty("mqtt.protocol", "ssl");
    public static final int MQTT_KEEPALIVE = Integer.parseInt(System.getProperty("mqtt.keepalive", "60"));
    public static final int MQTT_TIMEOUT = Integer.parseInt(System.getProperty("mqtt.timeout", "30"));

    // conproducer/device credentials
    public static final String TENANT_ID = System.getProperty("tenant", "DEFAULT_TENANT");
    public static final String DEVICE_ID = System.getProperty("device.id", UUID.randomUUID().toString());
    public static final String DEVICE_PASSWORD = System.getProperty("device.password", "my-secret-password");
    public static final String TRUSTSTORE_PATH = System.getProperty("truststore.path", "certs/truststore.pem");

    // other internal stuff
    //protected static final Logger logger = LoggerFactory.getLogger(MqttProducerBase.class);
    private final MqttClient client ;

    public MqttProducerBase() {
        client = createMqttApplicationClient() ;
    }

    protected void sendMessage() {
        
    }

    private MqttClient createMqttApplicationClient() {
        MqttClient aClient = null;
        String broker = MQTT_PROTOCOL + "://" + MQTT_HOST + ":" + MQTT_PORT;

        try {
            MemoryPersistence persistence = new MemoryPersistence();
            aClient = new MqttClient(broker, DEVICE_ID, persistence);

            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setKeepAliveInterval(MQTT_KEEPALIVE);
            connOpts.setConnectionTimeout(MQTT_TIMEOUT);
            connOpts.setMqttVersion(3);

            SSLSocketFactory socketFactory = SocketFactoryUtil.getInsecureSocketFactory(TRUSTSTORE_PATH);
			connOpts.setSocketFactory(socketFactory);
            connOpts.setUserName(DEVICE_ID + "@" + TENANT_ID);
            connOpts.setPassword(DEVICE_PASSWORD.toCharArray());

            System.out.println("Connecting to broker: "+broker);
            aClient.connect(connOpts);
            System.out.println("Connected to broker.");

        } catch(MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        }
        catch (Exception e) {
			e.printStackTrace();
		}

        return aClient ;   
    }
}