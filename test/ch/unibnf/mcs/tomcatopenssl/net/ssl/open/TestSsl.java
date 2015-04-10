/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.unibnf.mcs.tomcatopenssl.net.ssl.open;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.InetAddress;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.apache.catalina.Container;

import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Manager;
import org.apache.catalina.Server;
import org.apache.catalina.Service;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.AprLifecycleListener;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.session.ManagerBase;
import org.apache.catalina.session.StandardManager;
import org.apache.catalina.startup.CatalinaProperties;
import org.apache.catalina.startup.TesterServlet;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.startup.TomcatBaseTest;
import org.apache.catalina.valves.AccessLogValve;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.websocket.server.WsContextListener;
import org.junit.After;
import static org.junit.Assert.fail;
import org.junit.Before;

/**
 * The keys and certificates used in this file are all available in svn and were
 * generated using a test CA the files for which are in the Tomcat PMC private
 * repository since not all of them are AL2 licensed.
 */
public class TestSsl extends TomcatBaseTest {
    private Tomcat tomcat;
    private boolean accessLogEnabled = false;
    private File tempDir;
    
    @After
    @Override
    public void tearDown() throws Exception {
        try {
            // Some tests may call tomcat.destroy(), some tests may just call
            // tomcat.stop(), some not call either method. Make sure that stop()
            // & destroy() are called as necessary.
            if (tomcat.getServer() != null
                    && tomcat.getServer().getState() != LifecycleState.DESTROYED) {
                if (tomcat.getServer().getState() != LifecycleState.STOPPED) {
                    tomcat.stop();
                }
                tomcat.destroy();
            }
        } finally {
            
        }
    }
    
    @Before
    @Override
    public void setUp() throws Exception {
        tempDir = new File(System.getProperty("tomcat.test.temp", "output/tmp"));
        if (!tempDir.mkdirs() && !tempDir.isDirectory()) {
            fail("Unable to create temporary directory for test");
        }

        System.setProperty("catalina.base", tempDir.getAbsolutePath());

        // Get log instance after logging has been configured
        log = LogFactory.getLog(getClass());
        log.info("Starting test case [" + testName.getMethodName() + "]");
        CatalinaProperties.getProperty("foo");

        File appBase = new File(getTemporaryDirectory(), "webapps");
        if (!appBase.exists() && !appBase.mkdir()) {
            fail("Unable to create appBase for test");
        }
        tomcat = new TomcatWithFastSessionIDs();

        String protocol = getProtocol();
        Connector connector = new Connector(protocol);
        // Listen only on localhost
        connector.setAttribute("address",
                InetAddress.getByName("localhost").getHostAddress());
        // Use random free port
        connector.setPort(0);
        // Mainly set to reduce timeouts during async tests
        connector.setAttribute("connectionTimeout", "3000");
        tomcat.getService().addConnector(connector);
        tomcat.setConnector(connector);
        // Add AprLifecycleListener if we are using the Apr connector
        if (protocol.contains("Apr")) {
            StandardServer server = (StandardServer) tomcat.getServer();
            AprLifecycleListener listener = new AprLifecycleListener();
            listener.setSSLRandomSeed("/dev/urandom");
            server.addLifecycleListener(listener);
            connector.setAttribute("pollerThreadCount", Integer.valueOf(1));
        }

        File catalinaBase = tempDir;
        tomcat.setBaseDir(catalinaBase.getAbsolutePath());
        tomcat.getHost().setAppBase(appBase.getAbsolutePath());
        
        accessLogEnabled = Boolean.parseBoolean(
            System.getProperty("tomcat.test.accesslog", "false"));
        if (accessLogEnabled) {
            String accessLogDirectory = System
                    .getProperty("tomcat.test.reports");
            if (accessLogDirectory == null) {
                accessLogDirectory = new File(getBuildDirectory(), "logs")
                        .toString();
            }
            AccessLogValve alv = new AccessLogValve();
            alv.setDirectory(accessLogDirectory);
            alv.setPattern("%h %l %u %t \"%r\" %s %b %I %D");
            tomcat.getHost().getPipeline().addValve(alv);
        }

        // Cannot delete the whole tempDir, because logs are there,
        // but delete known subdirectories of it.
        addDeleteOnTearDown(new File(catalinaBase, "webapps"));
        addDeleteOnTearDown(new File(catalinaBase, "work"));
        System.out.println("setUp finished");
    }

    @Test
    public void testSimpleSsl() throws Exception {
        TesterSupport.configureClientSsl();
        System.out.println("SSL client configured");
        Tomcat tomcat = getTomcatInstance();
        System.out.println("Tomcat initialized");
        File appDir = new File(getBuildDirectory(), "webapps/examples");
        System.out.println("App dir: " + appDir.getAbsolutePath());
        org.apache.catalina.Context ctxt  = tomcat.addWebapp(
                null, "/examples", appDir.getAbsolutePath());
        System.out.println("aékBSékasbdéaksBDéasdé");
        ctxt.addApplicationListener(WsContextListener.class.getName());
        System.out.println("asdhéasdgéasdG");
        TesterSupport.initSsl(tomcat);
        System.out.println("SSL initialized");
        tomcat.start();
        ByteChunk res = getUrl("https://localhost:" + getPort() +
            "/examples/servlets/servlet/HelloWorldExample");
        assertTrue(res.toString().indexOf("<h1>Hello World!</h1>") > 0);
    }

    @Test
    public void testKeyPass() throws Exception {
        TesterSupport.configureClientSsl();

        Tomcat tomcat = getTomcatInstance();

        File appDir = new File(getBuildDirectory(), "webapps/examples");
        org.apache.catalina.Context ctxt  = tomcat.addWebapp(
                null, "/examples", appDir.getAbsolutePath());
        ctxt.addApplicationListener(WsContextListener.class.getName());

        TesterSupport.initSsl(tomcat, "localhost-copy1.jks", "changeit",
                "tomcatpass");

        tomcat.start();
        ByteChunk res = getUrl("https://localhost:" + getPort() +
            "/examples/servlets/servlet/HelloWorldExample");
        assertTrue(res.toString().indexOf("<h1>Hello World!</h1>") > 0);
    }


    @Test
    public void testRenegotiateWorks() throws Exception {
        Tomcat tomcat = getTomcatInstance();

        Assume.assumeTrue("SSL renegotiation has to be supported for this test",
                TesterSupport.isRenegotiationSupported(getTomcatInstance()));

        Context root = tomcat.addContext("", TEMP_DIR);
        Wrapper w =
            Tomcat.addServlet(root, "tester", new TesterServlet());
        w.setAsyncSupported(true);
        root.addServletMapping("/", "tester");

        TesterSupport.initSsl(tomcat);

        tomcat.start();

        SSLContext sslCtx = SSLContext.getInstance("TLS");
        sslCtx.init(null, TesterSupport.getTrustManagers(), null);
        SSLSocketFactory socketFactory = sslCtx.getSocketFactory();
        SSLSocket socket = (SSLSocket) socketFactory.createSocket("localhost",
                getPort());

        OutputStream os = socket.getOutputStream();
        InputStream is = socket.getInputStream();
        Reader r = new InputStreamReader(is);

        doRequest(os, r);

        TesterHandshakeListener listener = new TesterHandshakeListener();
        socket.addHandshakeCompletedListener(listener);

        socket.startHandshake();

        // One request should be sufficient
        int requestCount = 0;
        int listenerComplete = 0;
        try {
            while (requestCount < 10) {
                requestCount++;
                doRequest(os, r);
                if (listener.isComplete() && listenerComplete == 0) {
                    listenerComplete = requestCount;
                }
            }
        } catch (AssertionError | IOException e) {
            String message = "Failed on request number " + requestCount
                    + " after startHandshake(). " + e.getMessage();
            log.error(message, e);
            Assert.fail(message);
        }

        Assert.assertTrue(listener.isComplete());
        System.out.println("Renegotiation completed after " + listenerComplete + " requests");
    }

    private void doRequest(OutputStream os, Reader r) throws IOException {
        char[] expectedResponseLine = "HTTP/1.1 200 OK\r\n".toCharArray();

        os.write("GET /tester HTTP/1.1\r\n".getBytes());
        os.write("Host: localhost\r\n".getBytes());
        os.write("Connection: Keep-Alive\r\n\r\n".getBytes());
        os.flush();

        // First check we get the expected response line
        for (char c : expectedResponseLine) {
            int read = r.read();
            Assert.assertEquals(c, read);
        }

        // Skip to the end of the headers
        char[] endOfHeaders ="\r\n\r\n".toCharArray();
        int found = 0;
        while (found != endOfHeaders.length) {
            if (r.read() == endOfHeaders[found]) {
                found++;
            } else {
                found = 0;
            }
        }

        // Read the body
        char[] expectedBody = "OK".toCharArray();
        for (char c : expectedBody) {
            int read = r.read();
            Assert.assertEquals(c, read);
        }
    }

    private static class TesterHandshakeListener implements HandshakeCompletedListener {

        private volatile boolean complete = false;

        @Override
        public void handshakeCompleted(HandshakeCompletedEvent event) {
            complete = true;
        }

        public boolean isComplete() {
            return complete;
        }
    }
    
    private static class TomcatWithFastSessionIDs extends Tomcat {

        @Override
        public void start() throws LifecycleException {
            // Use fast, insecure session ID generation for all tests
            Server server = getServer();
            for (Service service : server.findServices()) {
                Container e = service.getContainer();
                for (Container h : e.findChildren()) {
                    for (Container c : h.findChildren()) {
                        Manager m = ((Context) c).getManager();
                        if (m == null) {
                            m = new StandardManager();
                            ((Context) c).setManager(m);
                        }
                        if (m instanceof ManagerBase) {
                            ((ManagerBase) m).setSecureRandomClass(
                                    "org.apache.catalina.startup.FastNonSecureRandom");
                        }
                    }
                }
            }
            super.start();
        }
    }
}
