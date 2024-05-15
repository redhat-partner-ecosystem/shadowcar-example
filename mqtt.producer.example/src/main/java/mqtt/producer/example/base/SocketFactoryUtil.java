package mqtt.producer.example.base;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class SocketFactoryUtil {

	public static final String SSL_CONTEXT = "TLSv1.3" ;

	public static SSLSocketFactory getSocketFactory(final String caCrtFile) throws Exception {
		Security.addProvider(new BouncyCastleProvider());

		// load CA certificate
		X509Certificate caCert = null;

		FileInputStream fis = new FileInputStream(caCrtFile);
		BufferedInputStream bis = new BufferedInputStream(fis);
		CertificateFactory cf = CertificateFactory.getInstance("X.509");

		while (bis.available() > 0) {
			caCert = (X509Certificate) cf.generateCertificate(bis);
		}

		// CA certificate is used to authenticate server
		KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
		caKs.load(null, null);
		caKs.setCertificateEntry("ca-certificate", caCert);
		TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
		tmf.init(caKs);

		// finally, create SSL socket factory
		SSLContext context = SSLContext.getInstance(SSL_CONTEXT);
		context.init(null, tmf.getTrustManagers(), null);

		return context.getSocketFactory();
	}

	public static SSLSocketFactory getInsecureSocketFactory(final String caCrtFile) throws Exception {
		Security.addProvider(new BouncyCastleProvider());
		final TrustManager[] trustAllCerts = new TrustManager[] { new InsecureTrustManager() };

		SSLContext context = SSLContext.getInstance(SSL_CONTEXT);
		context.init(null, trustAllCerts, new java.security.SecureRandom());

		return context.getSocketFactory();
	}

}