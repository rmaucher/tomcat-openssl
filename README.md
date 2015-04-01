# Openssl on Tomcat

studying OpenSsl and searching for a way to integrate it in Tomcat

The documentation related to the project is in another [repository](https://github.com/facenord-sud/tomcat-openssl-doc)

For now, the code is exploratory and may/should change a lot. It is a first try to see which part of the tc-netty and netty we need to take.

The code under `org.apache.tomcat.jni.*` is copied as is from the tc-netty. To compile it you need to follow the instructions provided here: https://github.com/netty/netty/wiki/Forked-Tomcat-Native

Now the owrk will be to detertmine which part of which code to use.

## Building

Actually, this project use Maven, because it is copied from tc-native-netty. The plan for building the project and to integrate with Tomcat is as follow:

* The plan is to build a JAR containing all the necessary code. This JAR can be included into the Tomcat `CLASSPATH` to use our SSL implementation.
* We use ant for managing the project (compiling Java and C code, running tests, genrating JAR, etc)
* We don't need to manage dependecies because we use the dependencies of Tomcat
* For compiling we add the path of the Tomcat sources and dependencies to the `CLASSPATH`, allowing us to utilize all the features of Tomcat.
* For now, a sample `build.xml` is provided, it will be updated when we will start to code.
