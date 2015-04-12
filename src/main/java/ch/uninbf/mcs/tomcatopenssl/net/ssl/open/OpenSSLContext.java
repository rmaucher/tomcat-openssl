/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.uninbf.mcs.tomcatopenssl.net.ssl.open;

import io.netty.handler.ssl.CipherSuiteConverter;
import io.netty.handler.ssl.OpenSsl;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.TrustManager;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.jni.Pool;
import org.apache.tomcat.jni.SSL;
import org.apache.tomcat.jni.SSLContext;
import org.apache.tomcat.util.net.SslContext;

/**
 *
 * @author leo
 */
public class OpenSSLContext extends SslContext {

    private static final String defaultProtocol = "TLS";

    private static final List<String> DEFAULT_CIPHERS;

    private List<String> ciphers = new ArrayList<>();

    public List<String> getCiphers() {
        return ciphers;
    }

    private List<String> requestedCiphers;

    public void setRequestedCiphers(List<String> ciphers) {
        this.requestedCiphers = ciphers;
    }

    private String enabledProtocol;

    public String getEnabledProtocol() {
        return enabledProtocol;
    }

    private final long aprPool;
    protected final long ctx;
    private static final Log logger = LogFactory.getLog(OpenSSLContext.class);

    static {
        List<String> ciphers = new ArrayList<>();
        // XXX: Make sure to sync this list with JdkSslEngineFactory.
        Collections.addAll(
                ciphers,
                "ECDHE-RSA-AES128-GCM-SHA256",
                "ECDHE-RSA-AES128-SHA",
                "ECDHE-RSA-AES256-SHA",
                "AES128-GCM-SHA256",
                "AES128-SHA",
                "AES256-SHA",
                "DES-CBC3-SHA",
                "RC4-SHA");
        DEFAULT_CIPHERS = Collections.unmodifiableList(ciphers);

        if (logger.isDebugEnabled()) {
            logger.debug("Default cipher suite (OpenSSL): " + ciphers);
        }
    }

    public OpenSSLContext() throws SSLException {
        OpenSsl.ensureAvailability();
        aprPool = Pool.create(0);

        try {
            synchronized (OpenSSLContext.class) {
                try {
                    ctx = SSLContext.make(aprPool, SSL.SSL_PROTOCOL_ALL, SSL.SSL_MODE_SERVER);
                } catch (Exception e) {
                    throw new SSLException("failed to create an SSL_CTX", e);
                }

                SSLContext.setOptions(ctx, SSL.SSL_OP_ALL);
                SSLContext.setOptions(ctx, SSL.SSL_OP_NO_SSLv2);
                SSLContext.setOptions(ctx, SSL.SSL_OP_NO_SSLv3);
                SSLContext.setOptions(ctx, SSL.SSL_OP_CIPHER_SERVER_PREFERENCE);
                SSLContext.setOptions(ctx, SSL.SSL_OP_SINGLE_ECDH_USE);
                SSLContext.setOptions(ctx, SSL.SSL_OP_SINGLE_DH_USE);
                SSLContext.setOptions(ctx, SSL.SSL_OP_NO_SESSION_RESUMPTION_ON_RENEGOTIATION);

//                /* List the ciphers that are permitted to negotiate. */
//                determineCiphers(null);
//                try {
//                    SSLContext.setCipherSuite(ctx, CipherSuiteConverter.toOpenSsl(this.ciphers));
//                } catch (SSLException e) {
//                    throw e;
//                } catch (Exception e) {
//                    throw new SSLException("failed to set cipher suite: " + this.ciphers, e);
//                }
//
//                List<String> nextProtoList = new ArrayList<>();
//                nextProtoList.add(defaultProtocol);
//                /* Set next protocols for next protocol negotiation extension, if specified */
//                
//                if (!nextProtoList.isEmpty()) {
//                    // Convert the protocol list into a comma-separated string.
//                    StringBuilder nextProtocolBuf = new StringBuilder();
//                    for (String p: nextProtoList) {
//                        nextProtocolBuf.append(p);
//                        nextProtocolBuf.append(',');
//                    }
//                    nextProtocolBuf.setLength(nextProtocolBuf.length() - 1);
//
//                    SSLContext.setNextProtos(ctx, nextProtocolBuf.toString());
//                }

                /* Set session cache size, if specified */
//                if (sessionCacheSize > 0) {
//                    this.sessionCacheSize = sessionCacheSize;
//                    SSLContext.setSessionCacheSize(ctx, sessionCacheSize);
//                } else {
//                    // Get the default session cache size using SSLContext.setSessionCacheSize()
//                    this.sessionCacheSize = sessionCacheSize = SSLContext.setSessionCacheSize(ctx, 20480);
//                    // Revert the session cache size to the default value.
//                    SSLContext.setSessionCacheSize(ctx, sessionCacheSize);
//                }
//
//                /* Set session timeout, if specified */
//                if (sessionTimeout > 0) {
//                    this.sessionTimeout = sessionTimeout;
//                    SSLContext.setSessionCacheTimeout(ctx, sessionTimeout);
//                } else {
//                    // Get the default session timeout using SSLContext.setSessionCacheTimeout()
//                    this.sessionTimeout = sessionTimeout = SSLContext.setSessionCacheTimeout(ctx, 300);
//                    // Revert the session timeout to the default value.
//                    SSLContext.setSessionCacheTimeout(ctx, sessionTimeout);
//                }
            }
//            success = true;
        } finally {
//            if (!success) {
//                destroyPools();
//            }
        }
    }

    private void determineCiphers(List<String> ciphers) {
        if (ciphers == null) {
            ciphers = DEFAULT_CIPHERS;
        }

        for (String c : ciphers) {
            if (c == null) {
                break;
            }
            String converted = CipherSuiteConverter.toOpenSsl(c);
            if (converted != null) {
                c = converted;
            }
            this.ciphers.add(c);
        }
    }

    @Override
    public void init(KeyManager[] kms, TrustManager[] tms, SecureRandom sr) throws KeyManagementException {
        synchronized (OpenSSLContext.class) {
            try {
                init();
            } catch (SSLException ex) {
                //TODO: catch exception
            }
        }
    }

    private void init() throws SSLException {
        determineCiphers(requestedCiphers);
        try {
            SSLContext.setCipherSuite(ctx, CipherSuiteConverter.toOpenSsl(this.ciphers));
        } catch (SSLException e) {
            throw e;
        } catch (Exception e) {
            throw new SSLException("failed to set cipher suite: " + this.ciphers, e);
        }
        
        
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
        if (protocol == null) {
            enabledProtocol = defaultProtocol;
        } else {
            enabledProtocol = protocol;
        }
    }

    void setCiphersWhish(List<String> endPointCiphers) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    void setrequestedCiphers(List<String> endpointCiphers) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
