# Openssl in Tomcat

> A new direct OpenSSL integration into Tomcat.

Right now, TLS/SSL encryption in Tomcat is managed in two ways. By the usage of the Java Secure Socket Extension(JSSE) API and by Tomcat native(tc-native) through APR and OpenSSL. Netty uses as well tc-native for providing is own OpenSSL implementation under Netty, called tc-netty.

This project will integrate OpenSLL into Tomcat but without depending of big projects like Tomcat native, APR or Netty. For integrating OpenSSL, we will use as most as possible the code from Tomcat and follow the same architecture as the JSSE implementation into Tomcat.

Our OpenSSL implementation will be available through the connector Nio2. In consequence, when using the Nio2 connector, users will have the choice between the JSSE implementation and OpenSSL.

**NOTE:** project in active development and can change a lot.

## Implementation

Currently, it is implemeneted under Tomcat 8.0.21 with some little changes in Tomcat.

A more detailed explanation can be found in the [wiki](https://github.com/facenord-sud/tomcat-openssl/wiki/OpenSSL-directly-into-Tomcat)

## Usage
 Ant is used for managing this project.
 
For building the Java part, you will need the [source code](http://tomcat.apache.org/download-80.cgi) of Tomcat 8.0.21. After downloading it, you need to build it. When it is done, clone this repository, build it and build again Tomcat.

For building the C part, you will need to have openssl and apr already installed.

When building this project, [some files](https://github.com/facenord-sud/tomcat-openssl/tree/master/src/main/java/org/apache/tomcat/util/net) will be copied to the Tomcat source code. The configuration of Tomcat under `output/build/conf` will be changed and the generated JAR's project will be added to the Tomcat's classpath.

### Details
1. [Build](https://tomcat.apache.org/tomcat-8.0-doc/building.html) Tomcat 8.0.21
2. Configure the file `build.properties` to indicate where Tomcat, OpenSSL and APR is located
3. Build the native C extension:
  * `cd src/main/c`
  * `./configure --with-apr=apr_install_path --with-ssl=openssl_install_path`
  * `make`
  * Read BUILDING
3. Run `ant deploy`, it will:
  * Copy some java files to Tomcat
  * Build the Java files of this project
  * Generate the JAR
  * Copy the JAR to `output/build/lib` of Tomcat
  * Change the configuration of `output/build/conf/server.xml` to use the Nio2 connector with our OpenSSL implementation
4. In Tomcat run `ant deploy` again, for compiling the modified/added files
5. In this project. run `ant run` to start Tomcat

## Code organization

The code under `org.apache.tomcat.tomcatopenssl.jni.*` is copied as is from the tc-netty. To compile it you need to follow the instructions provided here: https://github.com/netty/netty/wiki/Forked-Tomcat-Native

The code uner `io.netty.*` is copied from netty
