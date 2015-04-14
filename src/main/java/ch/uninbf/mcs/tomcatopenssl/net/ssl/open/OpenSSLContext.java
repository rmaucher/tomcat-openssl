/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.uninbf.mcs.tomcatopenssl.net.ssl.open;

import io.netty.handler.ssl.CipherSuiteConverter;
import io.netty.handler.ssl.OpenSsl;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
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
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.jni.CertificateVerifier;
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
    private static final Log log = LogFactory.getLog(OpenSSLContext.class);

    private static final List<String> DEFAULT_CIPHERS;
    private static final List<String> AVAILABLE_PROTOCOLS = new ArrayList<>();

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

    private long sessionCacheSize;

    public long getSessionCacheSize() {
        return sessionCacheSize;
    }

    public void setSessionCacheSize(long cacheSize) {
        this.sessionCacheSize = cacheSize;
    }

    private long sessionTimeout;

    public long getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(long sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
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
        Collections.addAll(AVAILABLE_PROTOCOLS, "SSLv3", "SSLv2", "TLSv1.2");

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
//            try {
//                init();
//                    KeyStore ks = KeyStore.getInstance("JKS");
//                    ks.load(null, null);
//                    CertificateFactory cf = CertificateFactory.getInstance("X.509");
//                    KeyFactory rsaKF = KeyFactory.getInstance("RSA");
//                    KeyFactory dsaKF = KeyFactory.getInstance("DSA");
//
//                    ByteBuf encodedKeyBuf = PemReader.readPrivateKey(keyFile);
//                    byte[] encodedKey = new byte[encodedKeyBuf.readableBytes()];
//                    encodedKeyBuf.readBytes(encodedKey).release();
//
//                    char[] keyPasswordChars = keyPassword.toCharArray();
//                    PKCS8EncodedKeySpec encodedKeySpec = generateKeySpec(keyPasswordChars, encodedKey);
//
//                    PrivateKey key;
//                    try {
//                        key = rsaKF.generatePrivate(encodedKeySpec);
//                    } catch (InvalidKeySpecException ignore) {
//                        key = dsaKF.generatePrivate(encodedKeySpec);
//                    }
//
//                    List<Certificate> certChain = new ArrayList<Certificate>();
//                    ByteBuf[] certs = PemReader.readCertificates(certChainFile);
//                    try {
//                        for (ByteBuf buf: certs) {
//                            certChain.add(cf.generateCertificate(new ByteBufInputStream(buf)));
//                        }
//                    } finally {
//                        for (ByteBuf buf: certs) {
//                            buf.release();
//                        }
//                    }
//
//                    ks.setKeyEntry("key", key, keyPasswordChars, certChain.toArray(new Certificate[certChain.size()]));
//
//                    if (trustManagerFactory == null) {
//                        // Mimic the way SSLContext.getInstance(KeyManager[], null, null) works
//                        trustManagerFactory = TrustManagerFactory.getInstance(
//                                TrustManagerFactory.getDefaultAlgorithm());
//                        trustManagerFactory.init((KeyStore) null);
//                    } else {
//                        trustManagerFactory.init(ks);
//                    }
//
//                    final X509TrustManager manager = chooseTrustManager(trustManagerFactory.getTrustManagers());
//                    SSLContext.setCertVerifyCallback(ctx, new CertificateVerifier() {
//                        @Override
//                        public boolean verify(long ssl, byte[][] chain, String auth) {
//                            X509Certificate[] peerCerts = certificates(chain);
//                            try {
//                                manager.checkClientTrusted(peerCerts, auth);
//                                return true;
//                            } catch (Exception e) {
//                                logger.debug("verification of certificate failed", e);
//                            }
//                            return false;
//                        }
//                    });
//            } catch (SSLException ex) {
//                //TODO: catch exception
//            } catch (IOException ex) {
//                Logger.getLogger(OpenSSLContext.class.getName()).log(Level.SEVERE, null, ex);
//            } catch (NoSuchAlgorithmException ex) {
//                Logger.getLogger(OpenSSLContext.class.getName()).log(Level.SEVERE, null, ex);
//            } catch (CertificateException ex) {
//                Logger.getLogger(OpenSSLContext.class.getName()).log(Level.SEVERE, null, ex);
//            }
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

        SSLContext.setNpnProtos(ctx, OpenSSLProtocols.getProtocols(enabledProtocol), SSL.SSL_SELECTOR_FAILURE_CHOOSE_MY_LAST_PROTOCOL);

        /* Set session cache size, if specified */
        if (sessionCacheSize > 0) {
            SSLContext.setSessionCacheSize(ctx, sessionCacheSize);
        } else {
            // Get the default session cache size using SSLContext.setSessionCacheSize()
            this.sessionCacheSize = SSLContext.setSessionCacheSize(ctx, 20480);
            // Revert the session cache size to the default value.
            SSLContext.setSessionCacheSize(ctx, sessionCacheSize);
        }

        /* Set session timeout, if specified */
        if (sessionTimeout > 0) {
            SSLContext.setSessionCacheTimeout(ctx, sessionTimeout);
        } else {
            // Get the default session timeout using SSLContext.setSessionCacheTimeout()
            this.sessionTimeout = sessionTimeout = SSLContext.setSessionCacheTimeout(ctx, 300);
            // Revert the session timeout to the default value.
            SSLContext.setSessionCacheTimeout(ctx, sessionTimeout);
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
        log.error(protocol);
    }
}
