# Openssl on Tomcat

studying OpenSsl and searching for a way to integrate it in Tomcat

The documentation related to the project is in another [repository](https://github.com/facenord-sud/tomcat-openssl-doc)

For now, the code is exploratory and may/should change a lot. It is a first try to see which part of the tc-netty and netty we need to take.

The code under `org.apache.tomcat.jni.*` is copied as is from the tc-netty. To compile it you need to follow the instructions provided here: https://github.com/netty/netty/wiki/Forked-Tomcat-Native

Now the owrk will be to detertmine which part of which code to use.