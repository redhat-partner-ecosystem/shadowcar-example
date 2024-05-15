package mqtt.producer.example.base;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedTrustManager;

class InsecureTrustManager extends X509ExtendedTrustManager {

    @Override
    public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
        // TODO Auto-generated method stub
        //throw new UnsupportedOperationException("Unimplemented method 'checkClientTrusted'");
    }

    @Override
    public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
        // TODO Auto-generated method stub
        //throw new UnsupportedOperationException("Unimplemented method 'checkServerTrusted'");
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAcceptedIssuers'");
    }

    @Override
    public void checkClientTrusted(X509Certificate[] arg0, String arg1, java.net.Socket arg2)
            throws CertificateException {
        // TODO Auto-generated method stub
        //throw new UnsupportedOperationException("Unimplemented method 'checkClientTrusted'");
    }

    @Override
    public void checkClientTrusted(X509Certificate[] arg0, String arg1, SSLEngine arg2)
            throws CertificateException {
        // TODO Auto-generated method stub
        //throw new UnsupportedOperationException("Unimplemented method 'checkClientTrusted'");
    }

    @Override
    public void checkServerTrusted(X509Certificate[] arg0, String arg1, java.net.Socket arg2)
            throws CertificateException {
        // TODO Auto-generated method stub
        //throw new UnsupportedOperationException("Unimplemented method 'checkServerTrusted'");
    }

    @Override
    public void checkServerTrusted(X509Certificate[] arg0, String arg1, SSLEngine arg2)
            throws CertificateException {
        // TODO Auto-generated method stub
        //throw new UnsupportedOperationException("Unimplemented method 'checkServerTrusted'");
    }

}
