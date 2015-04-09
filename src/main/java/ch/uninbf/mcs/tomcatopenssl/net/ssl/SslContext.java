/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.uninbf.mcs.tomcatopenssl.net.ssl;

import java.security.KeyManagementException;
import java.security.SecureRandom;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.TrustManager;

/**
 *
 * @author leo
 */
public interface SslContext {
    
    public abstract SslContext getInstance(String protocol);
    public abstract void init(KeyManager[] kms, TrustManager[] tms, SecureRandom sr) throws KeyManagementException;
    public abstract SSLSessionContext getServerSessionContext();
    public abstract SSLEngine createSSLEngine();
    public abstract SSLServerSocketFactory getServerSocketFactory();
    public abstract SSLParameters getSupportedSSLParameters();
}
