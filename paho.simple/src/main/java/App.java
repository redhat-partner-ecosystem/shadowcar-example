package shadowcar.example.paho.simple;

import java.util.*;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class App {

    public static void main(String[] args) {

        int MQTT_KEEPALIVE = 60;
        String caCertFile = "ca.crt";
        String topic        = "t";
        String content      = "{\"temp\": 5}";
        int qos             = 1;
        String protocol     = "tcp"; // ssl
        String broker       = "shadowcar-hono-adapter-mqtt.shadowcar-hono.svc.cluster.local:8883";
        String clientId = UUID.randomUUID().toString();
        
        MemoryPersistence persistence = new MemoryPersistence();

        try {
            MqttClient sampleClient = new MqttClient(protocol + "://"+ broker, clientId, persistence);

            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setKeepAliveInterval( MQTT_KEEPALIVE);
            connOpts.setMqttVersion(3);

            SSLSocketFactory socketFactory = getSocketFactory(caCertFile);
			connOpts.setSocketFactory(socketFactory);
            
            System.out.println("Connecting to broker: "+broker);
            sampleClient.connect(connOpts);
            
            System.out.println("Connected");
            System.out.println("Publishing message: "+content);
            
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(qos);
            sampleClient.publish(topic, message);
            System.out.println("Message published");

            sampleClient.disconnect();
            System.out.println("Disconnected");
            System.exit(0);

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
    }

    private static SSLSocketFactory getSocketFactory(final String caCrtFile) throws Exception
	{
		Security.addProvider(new BouncyCastleProvider());

		// load CA certificate
		X509Certificate caCert = null;

		FileInputStream fis = new FileInputStream(caCrtFile);
		BufferedInputStream bis = new BufferedInputStream(fis);
		CertificateFactory cf = CertificateFactory.getInstance("X.509");

		while (bis.available() > 0)
		{
			caCert = (X509Certificate) cf.generateCertificate(bis);
			// System.out.println(caCert.toString());
		}

		// CA certificate is used to authenticate server
		KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
		caKs.load(null, null);
		caKs.setCertificateEntry("ca-certificate", caCert);
		TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
		tmf.init(caKs);

		// finally, create SSL socket factory
		SSLContext context = SSLContext.getInstance("TLSv1.2");
		context.init(null, tmf.getTrustManagers(), null);

		return context.getSocketFactory();
	}
}