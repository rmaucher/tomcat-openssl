/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.uninbf.mcs.tomcatopenssl.net.ssl.open;

import ch.uninbf.mcs.tomcatopenssl.net.OpenSSLEndpoint;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.LogManager;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509KeyManager;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.Constants;
import org.apache.tomcat.util.net.SSLUtil;
import org.apache.tomcat.util.net.ServerSocketFactory;
import org.apache.tomcat.util.net.SslContext;
import org.apache.tomcat.util.net.jsse.JSSEKeyManager;
import static org.apache.tomcat.util.net.jsse.JSSESocketFactory.DEFAULT_KEY_PASS;
import org.apache.tomcat.util.net.jsse.openssl.OpenSSLCipherConfigurationParser;
import org.apache.tomcat.util.res.StringManager;

/**
 *
 * @author leo
 */
public class OpenSSLSocketFactory implements SSLUtil, ServerSocketFactory{

    private final AbstractEndpoint<?> endpoint;
    public OpenSSLEndpoint getEndpoint() { return (OpenSSLEndpoint) endpoint; }
    private static final Log log = LogFactory.getLog(OpenSSLSocketFactory.class);
    
    private static final String defaultKeystoreFile
        = System.getProperty("user.home") + "/.keystore";
    
    public OpenSSLSocketFactory(AbstractEndpoint<?> endPoint) {
        this.endpoint = endPoint;
    }
    
    @Override
    public SslContext createSSLContext() throws Exception {
        log.error(endpoint.getSslProtocol());
        OpenSSLContext context = (OpenSSLContext) SslContext.getInstance("ch.uninbf.mcs.tomcatopenssl.net.ssl.open.OpenSSLContext", endpoint.getSslProtocol());
        String requestedCiphersStr = endpoint.getCiphers();
        List<String> requestedCiphers = null;
        if (requestedCiphersStr.indexOf(':') != -1) {
            requestedCiphers = OpenSSLCipherConfigurationParser.parseExpression(requestedCiphersStr);
        }
        context.setRequestedCiphers(requestedCiphers);
        context.setSessionTimeout(Long.parseLong(endpoint.getSessionTimeout()));
        context.setSessionCacheSize(Long.parseLong(endpoint.getSessionCacheSize()));
        
        return context;
    }

    @Override
    public KeyManager[] getKeyManagers() throws Exception {
        KeyManager[] managers = { new OpenSSLKeyManager(getEndpoint().getCertChainFile(), getEndpoint().getKeyFile()) };
        return managers;
    }
    
    private void checkFileExists(File file) throws IOException {
        if(!file.exists()) {
            log.error("The file " + file + " do not exists");
            throw new FileNotFoundException();
        }
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

    @Override
    public ServerSocket createSocket(int port) throws IOException, InstantiationException {
        return null;
    }

    @Override
    public ServerSocket createSocket(int port, int backlog) throws IOException, InstantiationException {
        return null;
    }

    @Override
    public ServerSocket createSocket(int port, int backlog, InetAddress ifAddress) throws IOException, InstantiationException {
        return null;
    }

    @Override
    public Socket acceptSocket(ServerSocket socket) throws IOException {
        return null;
    }

    @Override
    public void handshake(Socket sock) throws IOException {
    }
    
}
