/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.uninbf.mcs.tomcatopenssl.net.ssl.open;

import static ch.uninbf.mcs.tomcatopenssl.util.Utility.*;
import io.netty.handler.ssl.CipherSuiteConverter;
import io.netty.handler.ssl.OpenSsl;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
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
import javax.crypto.Cipher;
import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
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
import org.apache.tomcat.tomcatopenssl.jni.CertificateVerifier;
import org.apache.tomcat.tomcatopenssl.jni.Pool;
import org.apache.tomcat.tomcatopenssl.jni.SSL;
import org.apache.tomcat.tomcatopenssl.jni.SSLContext;
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
    
    private OpenSslServerSessionContext sessionContext;

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

    private String keyPassword;

    public String getKeyPassowrd() {
        return keyPassword;
    }

    public void setKeyPassowrd(String keyPassowrd) {
        this.keyPassword = keyPassowrd;
    }

    private final long aprPool;
    protected final long ctx;
    private static final Log logger = LogFactory.getLog(OpenSSLContext.class);
    static final CertificateFactory X509_CERT_FACTORY;

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

        try {
            X509_CERT_FACTORY = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            throw new IllegalStateException("unable to instance X.509 CertificateFactory", e);
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
            }
//            success = true;
        } finally {
//            if (!success) {
//                destroyPools();
//            }
        }
    }

    public void determineCiphers(List<String> ciphers) {
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
                // From the class of io.netty.handler.ssl.OpenSslServerContext:157-245
                // OpenSSL requires certificates in PEM formats, but the verfication of the certificate
                // is done with KeyManagers and TrustManagers of Java
                // TODO-done: determine how to implemenet -> probably like netty
                // TODO-done: implement a similar way
                 /* Load the certificate file and private key. */
                if(kms != null) {
                    init(kms);
                }
                if(tms != null) {
                    init(tms);
                }
                sessionContext = new OpenSslServerSessionContext(ctx);
            } catch (SSLException ex) {
                //TODO: catch exception
            }
        }
    }
    
    private void init(TrustManager[] tms) {
        try {

            final X509TrustManager manager = chooseTrustManager(tms);
            SSLContext.setCertVerifyCallback(ctx, new CertificateVerifier() {
                @Override
                public boolean verify(long ssl, byte[][] chain, String auth) {
                    X509Certificate[] peerCerts = certificates(chain);
                    try {
                        manager.checkClientTrusted(peerCerts, auth);
                        return true;
                    } catch (Exception e) {
                        logger.debug("verification of certificate failed", e);
                    }
                    return false;
                }
            });
        } catch (Exception e) {
        }
    }

    private void init(KeyManager[] kms) throws SSLException {
        File certChainFile = null;
        File keyFile = null;
        try {
            certChainFile = chooseKeyManager(kms).getCertificateChain();
            keyFile = chooseKeyManager(kms).getPrivateKey();
            checkNotNull(keyPassword);
            if (!SSLContext.setCertificate(ctx, certChainFile.getPath(), keyFile.getPath(), keyPassword, SSL.SSL_AIDX_RSA)) {
                long error = SSL.getLastErrorNumber();
                if (OpenSsl.isError(error)) {
                    String err = SSL.getErrorString(error);
                    throw new SSLException("failed to set certificate: "
                            + certChainFile + " and " + keyFile + " (" + err + ')');
                }
            }
        } catch (SSLException e) {
            throw e;
        } catch (Exception e) {
            throw new SSLException("failed to set certificate: " + certChainFile + " and " + keyFile, e);
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
    
    static OpenSSLKeyManager chooseKeyManager(KeyManager[] managers) throws Exception {
        for (KeyManager manager : managers) {
            if (manager instanceof OpenSSLKeyManager) {
                return (OpenSSLKeyManager) manager;
            }
        }
        //TODO: find a more appropriate exception
        throw new IllegalStateException("No OpenSSLKeyManager found");
    }

    static X509TrustManager chooseTrustManager(TrustManager[] managers) {
        for (TrustManager m : managers) {
            if (m instanceof X509TrustManager) {
                return (X509TrustManager) m;
            }
        }
        throw new IllegalStateException("no X509TrustManager found");
    }

    private static X509Certificate[] certificates(byte[][] chain) {
        X509Certificate[] peerCerts = new X509Certificate[chain.length];
        for (int i = 0; i < peerCerts.length; i++) {
            peerCerts[i] = new OpenSslX509Certificate(chain[i]);
        }
        return peerCerts;
    }

    @Override
    public SSLSessionContext getServerSessionContext() {
        return sessionContext;
    }

    @Override
    public SSLEngine createSSLEngine() {
         return new OpenSslEngine(ctx, defaultProtocol, false, sessionContext);
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

    /**
     * Generates a key specification for an (encrypted) private key.
     *
     * @param password characters, if {@code null} or empty an unencrypted key
     * is assumed
     * @param key bytes of the DER encoded private key
     *
     * @return a key specification
     *
     * @throws IOException if parsing {@code key} fails
     * @throws NoSuchAlgorithmException if the algorithm used to encrypt
     * {@code key} is unkown
     * @throws NoSuchPaddingException if the padding scheme specified in the
     * decryption algorithm is unkown
     * @throws InvalidKeySpecException if the decryption key based on
     * {@code password} cannot be generated
     * @throws InvalidKeyException if the decryption key based on
     * {@code password} cannot be used to decrypt {@code key}
     * @throws InvalidAlgorithmParameterException if decryption algorithm
     * parameters are somehow faulty
     */
    protected static PKCS8EncodedKeySpec generateKeySpec(char[] password, byte[] key)
            throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException,
            InvalidKeyException, InvalidAlgorithmParameterException {

        if (password == null || password.length == 0) {
            return new PKCS8EncodedKeySpec(key);
        }

        EncryptedPrivateKeyInfo encryptedPrivateKeyInfo = new EncryptedPrivateKeyInfo(key);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(encryptedPrivateKeyInfo.getAlgName());
        PBEKeySpec pbeKeySpec = new PBEKeySpec(password);
        SecretKey pbeKey = keyFactory.generateSecret(pbeKeySpec);

        Cipher cipher = Cipher.getInstance(encryptedPrivateKeyInfo.getAlgName());
        cipher.init(Cipher.DECRYPT_MODE, pbeKey, encryptedPrivateKeyInfo.getAlgParameters());

        return encryptedPrivateKeyInfo.getKeySpec(cipher);
    }
}
