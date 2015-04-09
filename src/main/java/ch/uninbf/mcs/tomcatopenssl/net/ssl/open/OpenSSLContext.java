/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.uninbf.mcs.tomcatopenssl.net.ssl.open;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.TrustManager;
import org.apache.tomcat.util.net.SslContext;

/**
 *
 * @author leo
 */
public class OpenSSLContext extends SslContext{
    private static final String defaultProtocol = "TLS";
    private String enabledProtocol;

    @Override
    public void init(KeyManager[] kms, TrustManager[] tms, SecureRandom sr) throws KeyManagementException {
        
    }

    @Override
    public SSLSessionContext getServerSessionContext() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SSLEngine createSSLEngine() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SSLServerSocketFactory getServerSocketFactory() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SSLParameters getSupportedSSLParameters() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void initiateProtocol(String protocol) throws NoSuchAlgorithmException {
        if(protocol == null) {
            enabledProtocol = defaultProtocol;
        } else {
            enabledProtocol = protocol;
        }    
    }
}
