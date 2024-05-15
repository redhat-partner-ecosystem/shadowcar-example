package mqtt.producer.example.base;

import java.util.UUID;

import javax.net.ssl.SSLSocketFactory;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public abstract class MqttProducerBase {

    // connectivity to the MQTt adapter
    public static final String PRODUCER_MESSAGING_HOST = System.getProperty("producer.messaging.host", "shadowcar-hono-adapter-mqtt.shadowcar-hono.svc.cluster.local");
    public static final String PRODUCER_MESSAGING_PROTOCOL = System.getProperty("producer.messaging.protocol", "ssl");
    public static final int PRODUCER_MESSAGING_PORT = Integer.parseInt(System.getProperty("producer.messaging.port", "8883"));
    
    // conproducer/device credentials
    public static final String DEVICE_ID = System.getProperty("producer.device", UUID.randomUUID().toString());
    public static final String PRODUCER_USERNAME = System.getProperty("producer.username", "");
    public static final String PRODUCER_PASSWORD = System.getProperty("producer.password", "my-secret-password");
    
    public static final String TENANT_ID = System.getProperty("producer.tenant", "DEFAULT_TENANT");
    public static final String TRUSTSTORE_PATH = System.getProperty("truststore.path", "certs/truststore.pem");
    public static final int MQTT_KEEPALIVE = Integer.parseInt(System.getProperty("mqtt.keepalive", "60"));

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
        String broker = PRODUCER_MESSAGING_PROTOCOL + "://" + PRODUCER_MESSAGING_HOST + ":" + PRODUCER_MESSAGING_PORT;

        try {
            MemoryPersistence persistence = new MemoryPersistence();
            aClient = new MqttClient(broker, DEVICE_ID, persistence);

            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setKeepAliveInterval(MQTT_KEEPALIVE);
            connOpts.setMqttVersion(3);

            SSLSocketFactory socketFactory = SocketFactoryUtil.getInsecureSocketFactory(TRUSTSTORE_PATH);
			connOpts.setSocketFactory(socketFactory);
            
            System.out.println("Connecting to broker: "+broker);
            aClient.connect(connOpts);

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