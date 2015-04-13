# Openssl in Tomcat

> A new direct OpenSSL integration into Tomcat.

Right now, TLS/SSL encryption in Tomcat is managed in two ways. By the usage of the Java Secure Socket Extension(JSSE) API and by Tomcat native(tc-native) through APR and OpenSSL. Netty uses as well tc-native for providing is own OpenSSL implementation under Netty, called tc-netty.

This project will integrate OpenSLL into Tomcat but without depending of big projects like APR or Netty. For integrating OpenSSL, we will use as most as possible the code from Tomcat and follow the same architecture as the JSSE implementation into Tomcat.

Our OpenSSL implementation will be available through the connector Nio2. In consequence, when using the Nio2 connector, users will have the choice between the JSSE implementation and OpenSSL.

**NOTE:** project in active development and can change a lot.

## Implementation

Currently, it is implemeneted under Tomcat 8.0.21 with some little changes in Tomcat.

A more detailed explanation can be found in the [wiki](https://github.com/facenord-sud/tomcat-openssl/wiki/OpenSSL-directly-into-Tomcat)

## Usage
 Ant is used for managing this project.
 
For building the Java part, you will need the [source code](http://tomcat.apache.org/download-80.cgi) of Tomcat 8.0.21. After downloading it, you need to build it. When it is done, clone this repository, build it and build again Tomcat.

When building this project, [some files](https://github.com/facenord-sud/tomcat-openssl/tree/master/src/main/java/org/apache/tomcat/util/net) will be copied to the Tomcat source code. The configuration of Tomcat under `output/build/conf` will be changed and the generated JAR's project will be added to the Tomcat's classpath.

### Details


## Code organization

The code under `org.apache.tomcat.jni.*` is copied as is from the tc-netty. To compile it you need to follow the instructions provided here: https://github.com/netty/netty/wiki/Forked-Tomcat-Native

The code uner `io.netty.*` is copied from netty
