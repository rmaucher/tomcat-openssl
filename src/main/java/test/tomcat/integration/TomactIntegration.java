/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.tomcat.integration;

import org.apache.coyote.http11.AbstractHttp11JsseProtocol;
import org.apache.coyote.http11.Http11Nio2Protocol;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.Nio2Channel;

/**
 *
 * @author leo
 */
public class TomactIntegration extends Http11Nio2Protocol{

    private static final Log log = LogFactory.getLog(Http11Nio2Protocol.class);
    public TomactIntegration() {
        super();
        log.error("Hello world!");
    }

    @Override
    protected String getNamePrefix() {
        return "http-openssl";
    }

    
    
}