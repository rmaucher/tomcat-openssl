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
 * A demonstration of how to create a new protocol.
 * To use it:
 * 1. run ant build and copy the generated JAR to the lib path of the tomcat build.
 * 2. Modify the file conf/server.xml to use this new "protocol" (test.tomcat.integration.TomactIntegration)
 * 3. start Tomcat and search Hello world! through the logs
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