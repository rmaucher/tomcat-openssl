# Openssl in Tomcat

> A new direct OpenSSL integration into Tomcat.

Right now, TLS/SSL encryption in Tomcat is managed in two ways. By the usage of the Java Secure Socket Extension(JSSE) API and by Tomcat native(tc-native) through APR and OpenSSL. Netty uses as well tc-native for providing is own OpenSSL implementation under Netty, called tc-netty.

This project will integrate OpenSLL into Tomcat but without depending of big projects like APR or Netty. For integrating OpenSSL, we will use as most as possible the code from Tomcat and follow the same architecture as the JSSE implementation into Tomcat.

Our OpenSSL implementation will be available through the connector Nio2. In conseuquence, when using the Nio2 connector, users will have the choice between the JSSE implementation and OpenSSL.


## Building

Actually, this project use Maven, because it is copied from tc-native-netty. The plan for building the project and to integrate with Tomcat is as follow:

* The plan is to build a JAR containing all the necessary code. This JAR can be included into the Tomcat `CLASSPATH` to use our SSL implementation.
* We use ant for managing the project (compiling Java and C code, running tests, genrating JAR, etc)
* We don't need to manage dependecies because we use the dependencies of Tomcat
* For compiling we add the path of the Tomcat sources and dependencies to the `CLASSPATH`, allowing us to utilize all the features of Tomcat.
* For now, a sample `build.xml` is provided, it will be updated when we will start to code.
* 

## Code organization

The code under `org.apache.tomcat.jni.*` is copied as is from the tc-netty. To compile it you need to follow the instructions provided here: https://github.com/netty/netty/wiki/Forked-Tomcat-Native

The code uner `io.netty.*` is copied from netty
