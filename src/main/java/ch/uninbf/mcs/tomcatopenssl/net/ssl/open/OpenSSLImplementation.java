/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.uninbf.mcs.tomcatopenssl.net.ssl.open;

import javax.net.ssl.SSLSession;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.SSLImplementation;
import org.apache.tomcat.util.net.SSLSupport;
import org.apache.tomcat.util.net.SSLUtil;

/**
 *
 * @author leo
 */
public class OpenSSLImplementation extends SSLImplementation{

    @Override
    public String getImplementationName() {
        return "OpenSSl";
    }

    @Override
    public SSLSupport getSSLSupport(SSLSession session) {
        return new OpenSSLSupport(session);
    }

    @Override
    public SSLUtil getSSLUtil(AbstractEndpoint<?> ep) {
        return new OpenSSLSocketFactory();
    }
    
}
