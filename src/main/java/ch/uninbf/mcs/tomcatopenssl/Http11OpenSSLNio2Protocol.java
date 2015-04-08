/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.uninbf.mcs.tomcatopenssl;

import org.apache.coyote.http11.Http11Nio2Protocol;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

/**
 *
 * @author leo
 */
public class Http11OpenSSLNio2Protocol extends Http11Nio2Protocol{
    
    private static final Log log = LogFactory.getLog(Http11Nio2Protocol.class);
    public Http11OpenSSLNio2Protocol() {
        super();
        getEndpoint().setSslImplementationName("OpenSSL");
        log.error("Hello world!");
    }

    @Override
    protected String getNamePrefix() {
        return "http-openssl";
    }
}
