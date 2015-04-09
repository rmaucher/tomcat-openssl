/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.uninbf.mcs.tomcatopenssl.net.ssl.open;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.TrustManager;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.SSLUtil;
import org.apache.tomcat.util.net.SslContext;

/**
 *
 * @author leo
 */
public class OpenSSLSocketFactory implements SSLUtil{

    private final AbstractEndpoint<?> endpoint;
    
    public OpenSSLSocketFactory(AbstractEndpoint<?> endPoint) {
        this.endpoint = endPoint;
    }
    
    @Override
    public SslContext createSSLContext() throws Exception {
        return SslContext.getInstance("ch.uninbf.mcs.tomcatopenssl.net.ssl.open.OpenSSLContext", endpoint.getSslProtocol());
    }

    @Override
    public KeyManager[] getKeyManagers() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public TrustManager[] getTrustManagers() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void configureSessionContext(SSLSessionContext sslSessionContext) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String[] getEnableableCiphers(SslContext context) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String[] getEnableableProtocols(SslContext context) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
